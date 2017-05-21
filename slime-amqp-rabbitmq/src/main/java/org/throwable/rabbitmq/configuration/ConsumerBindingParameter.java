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
}
