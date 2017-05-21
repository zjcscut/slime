package org.throwable.lock.support;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/18 0:21
 */
public class RedisDistributedLock implements DistributedLock {

    private RLock rLock;

    public RedisDistributedLock(RLock rLock) {
        this.rLock = rLock;
    }

    @Override
    public void lock() throws Exception {
        rLock.lock();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws Exception {
        return rLock.tryLock(time, unit);
    }

    @Override
    public void release() throws Exception {
        rLock.unlock();
    }

    @Override
    public boolean isLocked() {
        return rLock.isLocked();
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return rLock.isHeldByCurrentThread();
    }

    @Override
    public void forceUnlock() {
        rLock.forceUnlock();
    }
}
