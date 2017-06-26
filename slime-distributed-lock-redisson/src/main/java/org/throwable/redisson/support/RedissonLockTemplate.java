package org.throwable.redisson.support;

import org.redisson.api.RLock;
import org.throwable.distributed.exception.LockException;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.TimeUnit;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/25 19:34
 */
public class RedissonLockTemplate {

	private RedissonLockFactory redissonLockFactory;

	public RedissonLockTemplate(RedissonLockFactory redissonLockFactory) {
		this.redissonLockFactory = redissonLockFactory;
	}

	public RedissonLockFactory getRedissonLockFactory() {
		return redissonLockFactory;
	}

	public void setRedissonLockFactory(RedissonLockFactory redissonLockFactory) {
		this.redissonLockFactory = redissonLockFactory;
	}

	public <T> T execute(String lockPath, long waitTime, long leaseTime, TimeUnit unit, RedissonLockCallback<T> callback) {
		return execute(lockPath, waitTime, leaseTime, unit, false, callback);
	}

	public <T> T execute(String lockPath, long waitTime, long leaseTime, TimeUnit unit, boolean isFair, RedissonLockCallback<T> callback) {
		RLock rLock = redissonLockFactory.createLockInstance(lockPath, isFair);
		boolean tryLockSuccess = false;
		try {
			tryLockSuccess = rLock.tryLock(waitTime, leaseTime, unit);
			if (tryLockSuccess) {
				return callback.doInLock();
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Error err) {
			throw err;
		} catch (Throwable ex) {
			throw new UndeclaredThrowableException(ex, "RedissonLockTemplate execute RedissonLockCallback threw undeclared checked exception");
		} finally {
			if (tryLockSuccess) {
				rLock.unlock();
			}
		}
		throw new LockException("RedissonLockTemplate try to acquire lock failed,lockPath:" + lockPath);
	}

}
