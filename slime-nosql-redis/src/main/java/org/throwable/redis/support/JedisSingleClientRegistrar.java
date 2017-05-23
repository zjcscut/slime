package org.throwable.redis.support;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.util.Assert;
import org.throwable.redis.configuration.RedisProperties;
import org.throwable.utils.EnvironmentUtils;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/5/11 11:43
 */
public class JedisSingleClientRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

	private RedisProperties redisProperties;

	@Override
	public void setEnvironment(Environment environment) {
		redisProperties = EnvironmentUtils.parseEnvironmentPropertiesToBean(environment, RedisProperties.class, RedisProperties.prefix);
		Assert.notNull(redisProperties, "Slime redis client configuration properties must not be null!");
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
										BeanDefinitionRegistry beanDefinitionRegistry) {
		registerJedisConnectionFactory(beanDefinitionRegistry);
	}

	private void registerJedisConnectionFactory(BeanDefinitionRegistry beanDefinitionRegistry) {
		ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) beanDefinitionRegistry;
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(redisProperties.getPool().getMaxActive());
		poolConfig.setMaxIdle(redisProperties.getPool().getMaxIdle());
		poolConfig.setMinIdle(redisProperties.getPool().getMinIdle());
		poolConfig.setMaxWaitMillis(redisProperties.getPool().getMaxWait());
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
		jedisConnectionFactory.setPoolConfig(poolConfig);
		jedisConnectionFactory.setHostName(redisProperties.getHost());
		jedisConnectionFactory.setPort(redisProperties.getPort());
		jedisConnectionFactory.setTimeout(redisProperties.getTimeout());
		jedisConnectionFactory.setPassword(redisProperties.getPassword());
		jedisConnectionFactory.setUsePool(true);
		jedisConnectionFactory.setUseSsl(redisProperties.isSsl());
		beanFactory.registerSingleton("jedisConnectionFactory", jedisConnectionFactory);
		registerDefaultRedisTemplate(jedisConnectionFactory, beanFactory);
	}

	private void registerDefaultRedisTemplate(JedisConnectionFactory jedisConnectionFactory,
											  ConfigurableBeanFactory beanFactory) {
		RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(jedisConnectionFactory);
		redisTemplate.setDefaultSerializer(buildRedisTemplateSerializer());
		beanFactory.registerSingleton("defaultRedisTemplate", redisTemplate);
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
