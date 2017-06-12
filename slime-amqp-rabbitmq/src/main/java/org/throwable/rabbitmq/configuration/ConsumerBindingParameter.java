package org.throwable.rabbitmq.configuration;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 16:03
 */
public class ConsumerBindingParameter extends BindingParameter {

	private String listenerClassName;
	private Integer concurrentConsumers;
	private Integer maxConcurrentConsumers;
	private String acknowledgeMode;
	private String queueName;

	public ConsumerBindingParameter() {
	}

	public String getListenerClassName() {
		return listenerClassName;
	}

	public void setListenerClassName(String listenerClassName) {
		this.listenerClassName = listenerClassName;
	}

	public Integer getConcurrentConsumers() {
		return concurrentConsumers;
	}

	public void setConcurrentConsumers(Integer concurrentConsumers) {
		this.concurrentConsumers = concurrentConsumers;
	}

	public Integer getMaxConcurrentConsumers() {
		return maxConcurrentConsumers;
	}

	public void setMaxConcurrentConsumers(Integer maxConcurrentConsumers) {
		this.maxConcurrentConsumers = maxConcurrentConsumers;
	}

	public String getAcknowledgeMode() {
		return acknowledgeMode;
	}

	public void setAcknowledgeMode(String acknowledgeMode) {
		this.acknowledgeMode = acknowledgeMode;
	}

	@Override
	public String getQueueName() {
		return queueName;
	}

	@Override
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		ConsumerBindingParameter that = (ConsumerBindingParameter) o;

		if (!listenerClassName.equals(that.listenerClassName)) return false;
		return queueName.equals(that.queueName);
	}

	@Override
	public int hashCode() {
		int result = listenerClassName.hashCode();
		result = 31 * result + queueName.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "ConsumerBindingParameter{" +
				"listenerClassName='" + listenerClassName + '\'' +
				", exchangeName='" + exchangeName + '\'' +
				", concurrentConsumers=" + concurrentConsumers +
				", exchangeType='" + exchangeType + '\'' +
				", maxConcurrentConsumers=" + maxConcurrentConsumers +
				", routingKey='" + routingKey + '\'' +
				", acknowledgeMode='" + acknowledgeMode + '\'' +
				", description='" + description + '\'' +
				", queueName='" + queueName + '\'' +
				'}';
	}
}
