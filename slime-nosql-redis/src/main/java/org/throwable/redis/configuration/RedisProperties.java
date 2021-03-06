package org.throwable.redis.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description copy from {@link org.throwable.redis.configuration.RedisProperties}
 * @since 2017/5/23 2:00
 */
@ConfigurationProperties(prefix = RedisProperties.prefix)
public class RedisProperties {

	public static final String prefix = "slime.redis";

	/**
	 * Database index used by the connection factory.
	 */
	private int database = 0;

	/**
	 * Redis url, which will overrule host, port and password if set.
	 */
	private String url;

	/**
	 * Redis server host.
	 */
	private String host = "localhost";

	/**
	 * Login password of the redis server.
	 */
	private String password;

	/**
	 * Redis server port.
	 */
	private Integer port = 6379;

	/**
	 * Enable SSL.
	 */
	private Boolean ssl = false;

	/**
	 * Connection timeout in milliseconds.
	 */
	private Integer timeout;

	private org.throwable.redis.configuration.RedisProperties.Pool pool;

	private org.throwable.redis.configuration.RedisProperties.Sentinel sentinel;

	private org.throwable.redis.configuration.RedisProperties.Cluster cluster;

	public Integer getDatabase() {
		return this.database;
	}

	public void setDatabase(Integer database) {
		this.database = database;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getPort() {
		return this.port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Boolean isSsl() {
		return this.ssl;
	}

	public void setSsl(Boolean ssl) {
		this.ssl = ssl;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public Integer getTimeout() {
		return this.timeout;
	}

	public org.throwable.redis.configuration.RedisProperties.Sentinel getSentinel() {
		return this.sentinel;
	}

	public void setSentinel(org.throwable.redis.configuration.RedisProperties.Sentinel sentinel) {
		this.sentinel = sentinel;
	}

	public org.throwable.redis.configuration.RedisProperties.Pool getPool() {
		return this.pool;
	}

	public void setPool(org.throwable.redis.configuration.RedisProperties.Pool pool) {
		this.pool = pool;
	}

	public org.throwable.redis.configuration.RedisProperties.Cluster getCluster() {
		return this.cluster;
	}

	public void setCluster(org.throwable.redis.configuration.RedisProperties.Cluster cluster) {
		this.cluster = cluster;
	}

	/**
	 * Pool properties.
	 */
	public static class Pool {

		/**
		 * Max number of "idle" connections in the pool. Use a negative value to indicate
		 * an unlimited number of idle connections.
		 */
		private Integer maxIdle = 8;

		/**
		 * Target for the minimum number of idle connections to maIntegerain in the pool. This
		 * setting only has an effect if it is positive.
		 */
		private Integer minIdle = 0;

		/**
		 * Max number of connections that can be allocated by the pool at a given time.
		 * Use a negative value for no limit.
		 */
		private Integer maxActive = 8;

		/**
		 * Maximum amount of time (in milliseconds) a connection allocation should block
		 * before throwing an exception when the pool is exhausted. Use a negative value
		 * to block indefinitely.
		 */
		private Integer maxWait = -1;

		public Integer getMaxIdle() {
			return this.maxIdle;
		}

		public void setMaxIdle(Integer maxIdle) {
			this.maxIdle = maxIdle;
		}

		public Integer getMinIdle() {
			return this.minIdle;
		}

		public void setMinIdle(Integer minIdle) {
			this.minIdle = minIdle;
		}

		public Integer getMaxActive() {
			return this.maxActive;
		}

		public void setMaxActive(Integer maxActive) {
			this.maxActive = maxActive;
		}

		public Integer getMaxWait() {
			return this.maxWait;
		}

		public void setMaxWait(Integer maxWait) {
			this.maxWait = maxWait;
		}

	}

	/**
	 * Cluster properties.
	 */
	public static class Cluster {

		/**
		 * Comma-separated list of "host:port" pairs to bootstrap from. This represents an
		 * "initial" list of cluster nodes and is required to have at least one entry.
		 */
		private List<String> nodes;

		/**
		 * Maximum number of redirects to follow when executing commands across the
		 * cluster.
		 */
		private Integer maxRedirects;

		public List<String> getNodes() {
			return this.nodes;
		}

		public void setNodes(List<String> nodes) {
			this.nodes = nodes;
		}

		public Integer getMaxRedirects() {
			return this.maxRedirects;
		}

		public void setMaxRedirects(Integer maxRedirects) {
			this.maxRedirects = maxRedirects;
		}

	}

	/**
	 * Redis sentinel properties.
	 */
	public static class Sentinel {

		/**
		 * Name of Redis server.
		 */
		private String master;

		/**
		 * Comma-separated list of host:port pairs.
		 */
		private String nodes;

		public String getMaster() {
			return this.master;
		}

		public void setMaster(String master) {
			this.master = master;
		}

		public String getNodes() {
			return this.nodes;
		}

		public void setNodes(String nodes) {
			this.nodes = nodes;
		}

	}
}
