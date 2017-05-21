package org.throwable.rabbitmq.configuration;

import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 16:03
 */
public class RabbitmqConsumerProperties extends RabbitmqConsumerInstanceProperties {

	private List<ConsumerBindingParameter> consumerBindingParameters;

	public RabbitmqConsumerProperties() {
	}

	public List<ConsumerBindingParameter> getConsumerBindingParameters() {
		return consumerBindingParameters;
	}

	public void setConsumerBindingParameters(List<ConsumerBindingParameter> consumerBindingParameters) {
		this.consumerBindingParameters = consumerBindingParameters;
	}
}
