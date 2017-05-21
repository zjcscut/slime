package org.throwable.rabbitmq.configuration;

import org.junit.Test;
import org.throwable.utils.JacksonUtils;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 16:11
 */
public class SlimeRabbitmqPropertiesTest {

	@Test
	public void testPropertiesToJson() throws Exception {
		SlimeRabbitmqProperties properties = new SlimeRabbitmqProperties();
		List<RabbitmqProducerProperties> producers = new ArrayList<>(1);
		RabbitmqProducerProperties producerProperties = new RabbitmqProducerProperties();
		producerProperties.setVirtualHost("/");
		producerProperties.setHost("localhost");
		producerProperties.setPort(5672);
		producerProperties.setUsername("guest");
		producerProperties.setPassword("guest");
		producerProperties.setUseConfirmCallback(true);
		producerProperties.setUseReturnCallback(true);
		producerProperties.setMandatory(true);
		List<BindingParameter> bindingParameters = new ArrayList<>(1);
		BindingParameter bindingParameter = new BindingParameter();
		bindingParameter.setDescription("Description");
		bindingParameter.setQueueName("queue-1");
		bindingParameter.setExchangeName("exchange1");
		bindingParameter.setExchangeType("DIRECT");
		bindingParameter.setRoutingKey("queue-key-1");
		bindingParameters.add(bindingParameter);
		producerProperties.setBindingParameters(bindingParameters);
		producers.add(producerProperties);

		List<RabbitmqConsumerProperties> consumers = new ArrayList<>(1);
		RabbitmqConsumerProperties consumerProperties = new RabbitmqConsumerProperties();
		consumerProperties.setVirtualHost("/");
		consumerProperties.setHost("localhost");
		consumerProperties.setPort(5672);
		consumerProperties.setUsername("guest");
		consumerProperties.setPassword("guest");
		List<ConsumerBindingParameter> consumerBindingParameters = new ArrayList<>(1);
		ConsumerBindingParameter consumerBindingParameter = new ConsumerBindingParameter();
		consumerBindingParameter.setDescription("Description");
		consumerBindingParameter.setQueueName("queue-1");
		consumerBindingParameter.setExchangeName("exchange1");
		consumerBindingParameter.setExchangeType("DIRECT");
		consumerBindingParameter.setRoutingKey("queue-key-1");
		consumerBindingParameter.setConcurrentConsumers(1);
		consumerBindingParameter.setMaxConcurrentConsumers(20);
		consumerBindingParameter.setListenerClassName("org.throwable.listener.Listener");
		consumerBindingParameters.add(consumerBindingParameter);
		consumerProperties.setConsumerBindingParameters(consumerBindingParameters);
		consumers.add(consumerProperties);

		properties.setConsumers(consumers);
		properties.setProducers(producers);
		System.out.println(JacksonUtils.toJson(properties));
	}

	@Test
	public void testParseMqJson()throws Exception{
		SlimeRabbitmqProperties properties = JacksonUtils.parseFromJsonFile("classpath:mq.json",SlimeRabbitmqProperties.class);
		assertEquals(properties.getConsumers().size(),1);
		assertEquals(properties.getProducers().size(),1);
	}

}