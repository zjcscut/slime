package org.throwable.lock.support;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/5/17 17:28
 */
public interface DistributedLockFactory {

	 DistributedLock createDistributedLockByPath(String lockPath);


}
