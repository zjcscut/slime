package org.throwable.redisson.support;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/25 21:58
 */
public interface RedissonLockCallback<T> {

	T doInLock();
}
