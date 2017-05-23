package org.throwable.redis.support;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/5/11 11:43
 */
public class JedisSingleClientRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private static final String SPRING_REDIS_PREFIX = "spring.redis";
    private static String SPRING_REDIS_POOL_PREFIX = SPRING_REDIS_PREFIX + ".pool";
    private static final Integer DEFAULT_MAXACTIVE = 8;
    private static final Integer DEFAULT_MAXIDLE = 8;
    private static final Integer DEFAULT_MINIDLE = 0;
    private static final Integer DEFAULT_MAXWAIT = -1;
    private static final Integer DEFAULT_TIMEOUT = 0;
    private int timeOut;
    private int maxIdle;
    private int minIdle;
    private int maxActive;
    private int maxWait;

    @Override
    public void setEnvironment(Environment environment) {
        String timeOutStr = environment.getProperty(SPRING_REDIS_PREFIX + ".timeout");
        String maxIdleStr = environment.getProperty(SPRING_REDIS_POOL_PREFIX + ".max-idle");
        String minIdleStr = environment.getProperty(SPRING_REDIS_POOL_PREFIX + ".min-idle");
        String maxActiveStr = environment.getProperty(SPRING_REDIS_POOL_PREFIX + ".max-active");
        String maxWaitStr = environment.getProperty(SPRING_REDIS_POOL_PREFIX + ".max-wait");
        timeOut = null != timeOutStr ? Integer.valueOf(timeOutStr) : DEFAULT_TIMEOUT;
        maxIdle = null != maxIdleStr ? Integer.valueOf(maxIdleStr) : DEFAULT_MAXIDLE;
        minIdle = null != minIdleStr ? Integer.valueOf(minIdleStr) : DEFAULT_MINIDLE;
        maxActive = null != maxActiveStr ? Integer.valueOf(maxActiveStr) : DEFAULT_MAXACTIVE;
        maxWait = null != maxWaitStr ? Integer.valueOf(maxWaitStr) : DEFAULT_MAXWAIT;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
                                        BeanDefinitionRegistry beanDefinitionRegistry) {
        registerJedisConnectionFactory(beanDefinitionRegistry);
        registerDefaultRedisTemplate(beanDefinitionRegistry);
    }

    private void registerJedisConnectionFactory(BeanDefinitionRegistry beanDefinitionRegistry) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWait);
        BeanDefinition jedisConnectionFactory = BeanDefinitionBuilder
                .genericBeanDefinition(JedisConnectionFactory.class)
                .addConstructorArgValue(poolConfig)
                .addPropertyValue("timeout", timeOut)
                .getBeanDefinition();
        beanDefinitionRegistry.registerBeanDefinition("jedisConnectionFactory", jedisConnectionFactory);
    }

    private void registerDefaultRedisTemplate(BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinition redisTemplate = BeanDefinitionBuilder
                .genericBeanDefinition(RedisTemplate.class)
                .addPropertyValue("defaultSerializer", buildRedisTemplateSerializer())
                .addPropertyReference("connectionFactory","jedisConnectionFactory")
                .getBeanDefinition();
        beanDefinitionRegistry.registerBeanDefinition("defaultRedisTemplate", redisTemplate);
    }

    private Jackson2JsonRedisSerializer buildRedisTemplateSerializer() {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        return jackson2JsonRedisSerializer;
    }

}
