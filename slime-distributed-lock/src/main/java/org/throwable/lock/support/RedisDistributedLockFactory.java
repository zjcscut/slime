package org.throwable.lock.support;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.throwable.lock.configuration.DistributedLockProperties;

import java.io.InputStream;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/17 23:01
 */
@Slf4j
@Component
public class RedisDistributedLockFactory implements DistributedLockFactory, DisposableBean {

    private static final String DEFAULT_LOCK_KEY_PREFIX = "REDISSON_LOCK_KEY_";

    private static RedissonClient client;

    @Autowired
    private DistributedLockProperties distributedLockProperties;

    @Override
    public void destroy() throws Exception {
        if (null != client && !client.isShutdown()) {
            client.shutdown();
        }
    }

    public void lazyInitRedissonClient() {
        try {
            if (null == client) {
            	String location = distributedLockProperties.getRedissonConfigurationLocation();
				Assert.hasText(location, "Distributed lock redisson configuration location must not be blank!!!");
                InputStream inputStream = new ClassPathResource(location)
                        .getInputStream();
                Config config = Config.fromYAML(inputStream);
                client = Redisson.create(config);
            }
        } catch (Exception e) {
            log.warn("Initialize redisson client failed!!!!",e);
        }
    }

    @Override
    public DistributedLock createDistributedLockByPath(String lockPath) {
    	lazyInitRedissonClient();
        return new RedisDistributedLock(client.getLock(DEFAULT_LOCK_KEY_PREFIX + lockPath));
    }

    public RLock createRLockByPath(String lockPath) {
		lazyInitRedissonClient();
        return createRLockByPath(DEFAULT_LOCK_KEY_PREFIX, lockPath);
    }

    public RLock createRLockByPath(String prefix, String lockPath) {
		lazyInitRedissonClient();
        return client.getLock(prefix + lockPath);
    }
}
