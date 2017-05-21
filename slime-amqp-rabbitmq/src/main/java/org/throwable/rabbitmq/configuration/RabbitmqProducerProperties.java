package org.throwable.rabbitmq.configuration;

import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 15:41
 */
public class RabbitmqProducerProperties extends RabbitmqProducerInstanceProperties {

	private List<BindingParameter> bindingParameters;

	public RabbitmqProducerProperties() {
	}

	public List<BindingParameter> getBindingParameters() {
		return bindingParameters;
	}

	public void setBindingParameters(List<BindingParameter> bindingParameters) {
		this.bindingParameters = bindingParameters;
	}
}
