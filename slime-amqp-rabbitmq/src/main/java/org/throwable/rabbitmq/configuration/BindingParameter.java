package org.throwable.rabbitmq.configuration;

import lombok.NonNull;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 11:31
 */

public class BindingParameter {

	@NonNull
	protected String queueName;
	protected String exchangeName;
	protected String exchangeType;
	protected String routingKey;
	protected String description;

	public BindingParameter() {
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getExchangeName() {
		return exchangeName;
	}

	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
	}

	public String getExchangeType() {
		return exchangeType;
	}

	public void setExchangeType(String exchangeType) {
		this.exchangeType = exchangeType;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		BindingParameter target = (BindingParameter) obj;
		return target.getQueueName().equals(this.getQueueName());
	}
}
