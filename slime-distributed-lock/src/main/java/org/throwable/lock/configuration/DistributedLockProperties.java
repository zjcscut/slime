package org.throwable.lock.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/5/18 15:28
 */
@ConfigurationProperties(prefix = "distrubited.lock")
public class DistributedLockProperties {

    private String zookeeperConfigurationLocation;

    private String redissonConfigurationLocation;

    public String getZookeeperConfigurationLocation() {
        return zookeeperConfigurationLocation;
    }

    public void setZookeeperConfigurationLocation(String zookeeperConfigurationLocation) {
        this.zookeeperConfigurationLocation = zookeeperConfigurationLocation;
    }

	public String getRedissonConfigurationLocation() {
		return redissonConfigurationLocation;
	}

	public void setRedissonConfigurationLocation(String redissonConfigurationLocation) {
		this.redissonConfigurationLocation = redissonConfigurationLocation;
	}
}
