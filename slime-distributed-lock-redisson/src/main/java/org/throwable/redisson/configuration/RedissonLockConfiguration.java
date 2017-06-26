package org.throwable.redisson.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.throwable.redisson.support.RedissonLockFactory;
import org.throwable.redisson.support.RedissonLockTemplate;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/25 19:35
 */
@Configuration
public class RedissonLockConfiguration {

	@Bean
	@ConditionalOnBean(value = {RedissonLockFactory.class})
	public RedissonLockTemplate redissonLockTemplate(RedissonLockFactory redissonLockFactory) {
       return new RedissonLockTemplate(redissonLockFactory);
	}
}
