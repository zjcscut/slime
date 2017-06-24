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
	private static final String DEFAULT_MODE = "json";
	private static final String DEFAULT_DATASOURCE_BEANNAME = "dataSource";

	private String mode = DEFAULT_MODE;

	private String location;

	private String dataSourceBeanName = DEFAULT_DATASOURCE_BEANNAME;

	private Boolean skipListenerClassNotFoundException = false;

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

	public String getDataSourceBeanName() {
		return dataSourceBeanName;
	}

	public void setDataSourceBeanName(String dataSourceBeanName) {
		this.dataSourceBeanName = dataSourceBeanName;
	}

	public Boolean getSkipListenerClassNotFoundException() {
		return skipListenerClassNotFoundException;
	}

	public void setSkipListenerClassNotFoundException(Boolean skipListenerClassNotFoundException) {
		this.skipListenerClassNotFoundException = skipListenerClassNotFoundException;
	}
}
