package org.throwable.lock.support;

import java.util.concurrent.TimeUnit;

/**
 * @author throwable
 * @version v1.0
 * @function 分布式锁接口
 * @since 2017/5/16 12:36
 */
public interface DistributedLock {

	void lock() throws Exception;

	boolean tryLock(long time, TimeUnit unit) throws Exception;

	void release() throws Exception;

	boolean isLocked();

	boolean isHeldByCurrentThread();

	void forceUnlock();

}
