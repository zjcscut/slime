package org.throwable.rabbitmq.configuration;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 15:42
 */

public class RabbitmqInstanceProperties {

	protected String username;
	protected String password;
	protected String host;
	protected Integer port;
	protected String virtualHost;
	protected String instanceSign;
	protected String description;

	public RabbitmqInstanceProperties() {
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getVirtualHost() {
		return virtualHost;
	}

	public void setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
	}

	public String getInstanceSign() {
		return instanceSign;
	}

	public void setInstanceSign(String instanceSign) {
		this.instanceSign = instanceSign;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
