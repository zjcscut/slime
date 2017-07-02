package org.throwable.druid.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/30 0:42
 */
@ConfigurationProperties(prefix = SlimeDruidProperties.PREFIX)
public class SlimeDruidProperties {

	public static final String PREFIX = "slime.druid";

	private String location;

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
