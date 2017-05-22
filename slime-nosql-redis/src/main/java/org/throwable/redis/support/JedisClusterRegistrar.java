package org.throwable.redis.support;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/5/11 11:43
 */
public class JedisClusterRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

	private static final String SPRING_REDIS_PREFIX = "spring.redis";
	private static final String SPRING_REDIS_CLUSTER_PREFIX = "spring.redis.cluster";
	private static String SPRING_REDIS_CLUSTER_NODES_PREFIX = SPRING_REDIS_CLUSTER_PREFIX + ".nodes";
	private static String SPRING_REDIS_POOL_PREFIX = SPRING_REDIS_PREFIX + ".pool";
	private static final String LEFT_BRACKET = "[";
	private static final String RIGHT_BRACKET = "]";
	private static final Integer DEFAULT_MAXACTIVE = 8;
	private static final Integer DEFAULT_MAXIDLE = 8;
	private static final Integer DEFAULT_MINIDLE = 0;
	private static final Integer DEFAULT_MAXWAIT = -1;
	private static final Integer DEFAULT_MAX_REDIRECTS = 1;
	private Set<HostAndPort> hostAndPorts;
	private int timeOut;
	private int maxRedirects;
	private int maxIdle;
	private int minIdle;
	private int maxActive;
	private int maxWait;

	@Override
	public void setEnvironment(Environment environment) {
		int index = 0;
		Set<String> nodes = new HashSet<>();
		while (null != environment.getProperty(SPRING_REDIS_CLUSTER_NODES_PREFIX + LEFT_BRACKET + index + RIGHT_BRACKET)) {
			nodes.add(environment.getProperty(SPRING_REDIS_CLUSTER_NODES_PREFIX + LEFT_BRACKET + index + RIGHT_BRACKET));
			index++;
		}
		hostAndPorts = convertNodesStringToHostAndPort(nodes);
		String timeOutStr = environment.getProperty(SPRING_REDIS_PREFIX + ".timeout");
		String maxRedirectsStr = environment.getProperty(SPRING_REDIS_CLUSTER_PREFIX + ".max-redirects");
		timeOut = null != timeOutStr ? Integer.valueOf(timeOutStr) : 0;
		maxRedirects = null != maxRedirectsStr ? Integer.valueOf(maxRedirects) : DEFAULT_MAX_REDIRECTS;
		String maxIdleStr = environment.getProperty(SPRING_REDIS_POOL_PREFIX + ".max-idle");
		String minIdleStr = environment.getProperty(SPRING_REDIS_POOL_PREFIX + ".min-idle");
		String maxActiveStr = environment.getProperty(SPRING_REDIS_POOL_PREFIX + ".max-active");
		String maxWaitStr = environment.getProperty(SPRING_REDIS_POOL_PREFIX + ".max-wait");
		maxIdle = null != maxIdleStr ? Integer.valueOf(maxIdleStr) : DEFAULT_MAXIDLE;
		minIdle = null != minIdleStr ? Integer.valueOf(minIdleStr) : DEFAULT_MINIDLE;
		maxActive = null != maxActiveStr ? Integer.valueOf(maxActiveStr) : DEFAULT_MAXACTIVE;
		maxWait = null != maxWaitStr ? Integer.valueOf(maxWaitStr) : DEFAULT_MAXWAIT;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
										BeanDefinitionRegistry beanDefinitionRegistry) {
		ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) beanDefinitionRegistry;
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(maxActive);
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMinIdle(minIdle);
		poolConfig.setMaxWaitMillis(maxWait);
		BeanDefinition jedisClusterBean = BeanDefinitionBuilder
				.genericBeanDefinition(JedisCluster.class)
				.addConstructorArgValue(hostAndPorts)
				.addConstructorArgValue(timeOut)
				.addConstructorArgValue(maxRedirects)
				.addConstructorArgValue(poolConfig)
				.getBeanDefinition();
		beanFactory.registerSingleton("jedisCluster", jedisClusterBean);
	}

	private static Set<HostAndPort> convertNodesStringToHostAndPort(Set<String> nodes) {
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
