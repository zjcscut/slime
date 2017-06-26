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
	protected String instanceSignature;
	protected String description;

	//queue、exchange、routingKey suffix
	protected String suffix;

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

	public String getInstanceSignature() {
		return instanceSignature;
	}

	public void setInstanceSignature(String instanceSignature) {
		this.instanceSignature = instanceSignature;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RabbitmqInstanceProperties that = (RabbitmqInstanceProperties) o;
		if (host != null ? !host.equals(that.host) : that.host != null) return false;
		if (port != null ? !port.equals(that.port) : that.port != null) return false;
		return instanceSignature != null ? instanceSignature.equals(that.instanceSignature) : that.instanceSignature == null;
	}

	@Override
	public int hashCode() {
		int result = host != null ? host.hashCode() : 0;
		result = 31 * result + (port != null ? port.hashCode() : 0);
		result = 31 * result + (instanceSignature != null ? instanceSignature.hashCode() : 0);
		return result;
	}
}
