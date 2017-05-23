package org.throwable.redis.annotation;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.throwable.redis.configuration.RedisProperties;
import org.throwable.redis.support.JedisSingleClientRegistrar;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @function 启动redis单机客户端
 * @since 2017/5/11 20:01
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({JedisSingleClientRegistrar.class})
@EnableConfigurationProperties(RedisProperties.class)
public @interface EnableRedisClient {

}
