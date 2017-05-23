package org.throwable.redis.annotation;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.throwable.redis.configuration.RedisProperties;
import org.throwable.redis.support.JedisClusterRegistrar;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @function 启用redis集群
 * @since 2017/5/11 11:41
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({JedisClusterRegistrar.class})
@EnableConfigurationProperties(RedisProperties.class)
public @interface EnableRedisCluster {

}
