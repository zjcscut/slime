package org.throwable.redis.support;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.throwable.redis.configuration.RedisProperties;
import org.throwable.utils.EnvironmentUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/5/11 11:43
 */
public class JedisClusterRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

	private RedisProperties redisProperties;

	@Override
	public void setEnvironment(Environment environment) {
		redisProperties = EnvironmentUtils.parseEnvironmentPropertiesToBean(environment, RedisProperties.class, RedisProperties.prefix);
		Assert.notNull(redisProperties,"Slime redis cluster configuration properties must not be null!");
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
										BeanDefinitionRegistry beanDefinitionRegistry) {
		ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) beanDefinitionRegistry;
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(redisProperties.getPool().getMaxActive());
		poolConfig.setMaxIdle(redisProperties.getPool().getMaxIdle());
		poolConfig.setMinIdle(redisProperties.getPool().getMinIdle());
		poolConfig.setMaxWaitMillis(redisProperties.getPool().getMaxWait());
		Set<HostAndPort> hostAndPorts = convertNodesStringToHostAndPort(redisProperties.getCluster().getNodes());
		JedisCluster jedisCluster = new JedisCluster(hostAndPorts, redisProperties.getTimeout(),
				redisProperties.getCluster().getMaxRedirects(), poolConfig);
		beanFactory.registerSingleton("jedisCluster", jedisCluster);
	}

	private static Set<HostAndPort> convertNodesStringToHostAndPort(List<String> nodes) {
		Set<HostAndPort> hostAndPorts = new HashSet<>();
		if (null != nodes && !nodes.isEmpty()) {
			for (String node : nodes) {
				String[] nodeAndPort = node.split(":");
				hostAndPorts.add(new HostAndPort(nodeAndPort[0], Integer.valueOf(nodeAndPort[1])));
			}
		}
		return hostAndPorts;
	}
}
