package org.throwable.rabbitmq.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 15:45
 */
@ConfigurationProperties(prefix = RabbitmqProperties.PREFIX)
public class RabbitmqProperties {

	public static final String PREFIX = "slime.amqp.rabbitmq";
	public static final String DEFAULT_MODE = "json";

	private String mode = DEFAULT_MODE;

	private String location;

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
