package org.throwable.redisson.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/25 1:13
 */
@ConfigurationProperties(prefix = RedissonLockProperties.PREFIX)
public class RedissonLockProperties {

	public static final String PREFIX = "slime.distributed.lock.redisson";

	private String location;

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
