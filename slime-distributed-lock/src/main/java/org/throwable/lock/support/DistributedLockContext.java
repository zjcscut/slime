package org.throwable.lock.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.throwable.lock.common.LockPolicyEnum;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/17 23:02
 */
@Component
public class DistributedLockContext {

    @Autowired
    private ApplicationContext applicationContext;

    private static final Map<LockPolicyEnum, Class<? extends DistributedLockFactory>> repository =
            new EnumMap<>(LockPolicyEnum.class);

    static {
        repository.put(LockPolicyEnum.ZOOKEEPER, ZookeeperDistributedLockFactory.class);
        repository.put(LockPolicyEnum.REDIS, RedisDistributedLockFactory.class);
    }

    public DistributedLock getLockByPolicyAndPath(LockPolicyEnum policy, String path) {
        return applicationContext.getBean(repository.get(policy))
                .createDistributedLockByPath(path);
    }

    public DistributedLock getRedisLockByPath(String path) {
        return getLockByPolicyAndPath(LockPolicyEnum.REDIS, path);
    }

    public DistributedLock getZookeeperLockByPath(String path) {
        return getLockByPolicyAndPath(LockPolicyEnum.ZOOKEEPER, path);
    }
}
