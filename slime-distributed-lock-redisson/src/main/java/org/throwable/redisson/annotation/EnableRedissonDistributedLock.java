package org.throwable.redisson.annotation;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.throwable.redisson.configuration.RedissonLockProperties;
import org.throwable.redisson.support.RedissonLockAspectRegistrar;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/25 1:51
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(value = RedissonLockAspectRegistrar.class)
@EnableConfigurationProperties(value = RedissonLockProperties.class)
public @interface EnableRedissonDistributedLock {

}
