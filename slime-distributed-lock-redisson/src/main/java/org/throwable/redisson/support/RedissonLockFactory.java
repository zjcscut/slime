package org.throwable.redisson.support;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.throwable.redisson.configuration.RedissonLockProperties;
import org.throwable.redisson.exception.RedissoninitializationException;

import java.io.InputStream;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/25 2:18
 */
@Slf4j
@Component
public class RedissonLockFactory implements InitializingBean, DisposableBean {

	@Autowired
	private RedissonLockProperties redissonLockProperties;

	private RedissonClient client;

	@Override
	public void afterPropertiesSet() throws Exception {
		initRedissonClient();
	}

	@Override
	public void destroy() throws Exception {
		if (null != client && !client.isShutdown()) {
			client.shutdown();
		}
	}

	private void initRedissonClient() throws Exception {
		String location = redissonLockProperties.getLocation();
		Assert.hasText(location, "RedissonLockProperties field location must not be empty!");
		try {
			InputStream inputStream = new ClassPathResource(location).getInputStream();
			Config config = Config.fromYAML(inputStream);
			client = Redisson.create(config);
		} catch (Exception e) {
			log.error("Initialize redisson client failed!!!!", e);
			throw new RedissoninitializationException(e);
		}
	}

	public RLock createLockInstance(String realLockPath, boolean isFair) {
		if (isFair) {
			return client.getFairLock(realLockPath);
		} else {
			return client.getLock(realLockPath);
		}
	}
}
