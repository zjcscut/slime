package org.throwable.rabbitmq.configuration;

import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 15:59
 */
public class SlimeRabbitmqProperties {

	private List<RabbitmqProducerProperties> producers;
	private List<RabbitmqConsumerProperties> consumers;

	public SlimeRabbitmqProperties() {
	}

	public List<RabbitmqProducerProperties> getProducers() {
		return producers;
	}

	public void setProducers(List<RabbitmqProducerProperties> producers) {
		this.producers = producers;
	}

	public List<RabbitmqConsumerProperties> getConsumers() {
		return consumers;
	}

	public void setConsumers(List<RabbitmqConsumerProperties> consumers) {
		this.consumers = consumers;
	}

}
