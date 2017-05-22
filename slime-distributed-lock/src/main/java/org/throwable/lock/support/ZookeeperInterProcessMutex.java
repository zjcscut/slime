package org.throwable.lock.support;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.*;
import org.apache.curator.utils.PathUtils;
import org.throwable.lock.exception.LockException;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author throwable
 * @version v1.0
 * @description copy and extend from {@link InterProcessMutex}
 * @since 2017/5/17 22:12
 */
public class ZookeeperInterProcessMutex implements InterProcessLock, Revocable<ZookeeperInterProcessMutex> {

	private final ZookeeperLockInternals internals;
	private final String basePath;
	private CuratorFramework client;

	private final ConcurrentMap<Thread, LockData> threadData = Maps.newConcurrentMap();

	private static class LockData {
		final Thread owningThread;
		final String lockPath;
		final AtomicInteger lockCount = new AtomicInteger(1);

		private LockData(Thread owningThread, String lockPath) {
			this.owningThread = owningThread;
			this.lockPath = lockPath;
		}
	}

	private static final String LOCK_NAME = "lock-";

	/**
	 * @param client client
	 * @param path   the path to lock
	 */
	public ZookeeperInterProcessMutex(CuratorFramework client, String path) {
		this(client, path, new StandardLockInternalsDriver());
		this.client = client;
	}

	/**
	 * @param client client
	 * @param path   the path to lock
	 * @param driver lock driver
	 */
	public ZookeeperInterProcessMutex(CuratorFramework client, String path, LockInternalsDriver driver) {
		this(client, path, LOCK_NAME, 1, driver);
		this.client = client;
	}

	/**
	 * Acquire the mutex - blocking until it's available. Note: the same thread
	 * can call acquire re-entrantly. Each call to acquire must be balanced by a call
	 * to {@link #release()}
	 *
	 * @throws Exception ZK errors, connection interruptions
	 */
	@Override
	public void acquire() throws Exception {
		if (!internalLock(-1, null)) {
			throw new IOException("Lost connection while trying to acquire lock: " + basePath);
		}
	}

	/**
	 * Acquire the mutex - blocks until it's available or the given time expires. Note: the same thread
	 * can call acquire re-entrantly. Each call to acquire that returns true must be balanced by a call
	 * to {@link #release()}
	 *
	 * @param time time to wait
	 * @param unit time unit
	 * @return true if the mutex was acquired, false if not
	 * @throws Exception ZK errors, connection interruptions
	 */
	@Override
	public boolean acquire(long time, TimeUnit unit) throws Exception {
		return internalLock(time, unit);
	}

	/**
	 * Returns true if the mutex is acquired by a thread in this JVM
	 *
	 * @return true/false
	 */
	@Override
	public boolean isAcquiredInThisProcess() {
		return (threadData.size() > 0);
	}

	/**
	 * Perform one release of the mutex if the calling thread is the same thread that acquired it. If the
	 * thread had made multiple calls to acquire, the mutex will still be held when this method returns.
	 *
	 * @throws Exception ZK errors, interruptions, current thread does not own the lock
	 */
	@Override
	public void release() throws Exception {
		/*
			Note on concurrency: a given lockData instance
            can be only acted on by a single thread so locking isn't necessary
         */

		Thread currentThread = Thread.currentThread();
		ZookeeperInterProcessMutex.LockData lockData = threadData.get(currentThread);
		if (lockData == null) {
			throw new IllegalMonitorStateException("You do not own the lock: " + basePath);
		}

		int newLockCount = lockData.lockCount.decrementAndGet();
		if (newLockCount > 0) {
			return;
		}
		if (newLockCount < 0) {
			throw new IllegalMonitorStateException("Lock count has gone negative for lock: " + basePath);
		}
		try {
			internals.releaseLock(lockData.lockPath);
		} finally {
			threadData.remove(currentThread);
		}
	}

	/**
	 * Return a sorted list of all current nodes participating in the lock
	 *
	 * @return list of nodes
	 * @throws Exception ZK errors, interruptions, etc.
	 */
	public Collection<String> getParticipantNodes() throws Exception {
		return LockInternals.getParticipantNodes(internals.getClient(), basePath, internals.getLockName(), internals.getDriver());
	}

	/**
	 * MoreExecutors.newDirectExecutorService
	 * @since guava 18.0 (present as MoreExecutors.sameThreadExecutor() since guava 10.0)
	 */
	@Override
	public void makeRevocable(RevocationListener<ZookeeperInterProcessMutex> listener) {
		makeRevocable(listener, MoreExecutors.newDirectExecutorService());
	}

	@Override
	public void makeRevocable(final RevocationListener<ZookeeperInterProcessMutex> listener, Executor executor) {
		internals.makeRevocable(new RevocationSpec(executor, () -> listener.revocationRequested(ZookeeperInterProcessMutex.this)));
	}

	ZookeeperInterProcessMutex(CuratorFramework client, String path, String lockName, int maxLeases, LockInternalsDriver driver) {
		basePath = PathUtils.validatePath(path);
		internals = new ZookeeperLockInternals(client, driver, path, lockName, maxLeases);
		this.client = client;
	}

	public boolean isOwnedByCurrentThread() {
		ZookeeperInterProcessMutex.LockData lockData = threadData.get(Thread.currentThread());
		return (lockData != null) && (lockData.lockCount.get() > 0);
	}

	protected byte[] getLockNodeBytes() {
		return null;
	}

	protected String getLockPath() {
		ZookeeperInterProcessMutex.LockData lockData = threadData.get(Thread.currentThread());
		return lockData != null ? lockData.lockPath : null;
	}

	private boolean internalLock(long time, TimeUnit unit) throws Exception {
        /*
           Note on concurrency: a given lockData instance
           can be only acted on by a single thread so locking isn't necessary
        */

		Thread currentThread = Thread.currentThread();
		ZookeeperInterProcessMutex.LockData lockData = threadData.get(currentThread);
		if (lockData != null) {
			// re-entering
			lockData.lockCount.incrementAndGet();
			return true;
		}
		String lockPath = internals.attemptLock(time, unit, getLockNodeBytes());
		if (lockPath != null) {
			ZookeeperInterProcessMutex.LockData newLockData = new ZookeeperInterProcessMutex.LockData(currentThread, lockPath);
			threadData.put(currentThread, newLockData);
			return true;
		}
		return false;
	}

	public void forceUnlock(){
		try {
			this.client.delete().guaranteed().forPath(basePath);
		}catch (Exception e){
			throw new LockException("delete path and forceUnlock failed!!!!Path :" + basePath);
		}
	}

	public boolean isLocked() {
		try {
			return  null != this.client.checkExists().forPath(basePath);
		} catch (Exception e) {
			throw new LockException("check isLocked and checkExists failed!!!!Path :" + basePath);
		}
	}

}
