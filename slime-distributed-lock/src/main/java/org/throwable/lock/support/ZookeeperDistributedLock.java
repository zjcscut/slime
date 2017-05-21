package org.throwable.lock.support;

import java.util.concurrent.TimeUnit;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/17 23:20
 */
public class ZookeeperDistributedLock implements DistributedLock {

	private ZookeeperInterProcessMutex lock;

	public ZookeeperDistributedLock(ZookeeperInterProcessMutex lock) {
		this.lock = lock;
	}

	@Override
	public void lock() throws Exception {
		lock.acquire();
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws Exception {
		return lock.acquire(time, unit);
	}

	@Override
	public void release() throws Exception {
        lock.release();
	}

	@Override
	public boolean isLocked() {
       return lock.isLocked();
	}

	@Override
	public boolean isHeldByCurrentThread() {
		return lock.isOwnedByCurrentThread();
	}

	@Override
	public void forceUnlock() {
        lock.forceUnlock();
	}
}
