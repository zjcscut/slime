package org.throwable.rabbitmq.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.util.Assert;
import org.throwable.rabbitmq.common.ExchangeEnum;
import org.throwable.rabbitmq.configuration.BindingParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author throwable
 * @version 2017/1/16 16:15
 * @function rabbitmq超级管理员操作, 声明Queue、Exchange、Binding
 */

public class RabbitAdminOperator {

	private static final Logger logger = LoggerFactory.getLogger(RabbitAdminOperator.class);

	private RabbitAdmin rabbitAdmin;

	public RabbitAdminOperator(RabbitAdmin rabbitAdmin) {
		this.rabbitAdmin = rabbitAdmin;
	}

	public void init(List<BindingParameter> BindingParameter) {
		List<Queue> queues = buildQueues(BindingParameter);
		for (Queue queue : queues) {
			logger.debug("Rabbitmq AmqpAdmin declareQueue :" + queue.getName());
			rabbitAdmin.declareQueue(queue);
		}
		List<Exchange> exchanges = buildExchanges(BindingParameter);
		for (Exchange exchange : exchanges) {
			logger.debug("Rabbitmq AmqpAdmin declareExchange :" + exchange.getName());
			rabbitAdmin.declareExchange(exchange);
		}
		List<Binding> bindings = buildBindings(BindingParameter);
		for (Binding binding : bindings) {
			logger.debug("Rabbitmq AmqpAdmin declareBinding :" + binding.getDestination());
			rabbitAdmin.declareBinding(binding);
		}
	}

	private List<Queue> buildQueues(List<BindingParameter> BindingParameter) {
		List<Queue> queues = new ArrayList<>(BindingParameter.size());
		for (BindingParameter queue : BindingParameter) {
			queues.add(createQueue(queue.getQueueName()));
		}
		return queues;
	}

	/**
	 * 构造队列对象,默认持久化
	 *
	 * @param name name
	 * @return Queue
	 */
	private Queue createQueue(String name) {
		return new Queue(name, true);
	}


	private List<Binding> buildBindings(List<BindingParameter> BindingParameter) {
		List<Binding> bindings = new ArrayList<>(BindingParameter.size());
		for (BindingParameter bindingSingle : BindingParameter) {
			bindings.add(transferBinding(bindingSingle));
		}
		return bindings;
	}

	/**
	 * 根据Exchange类型进行Binding封装
	 *
	 * @param bindingSingle bindingSingle
	 * @return Binding
	 */
	private Binding transferBinding(BindingParameter bindingSingle) {
		Assert.hasText(bindingSingle.getExchangeType(), "ExchangeType must not be empty," + bindingSingle);
		String type = bindingSingle.getExchangeType().toUpperCase();
		Binding binding;
		switch (ExchangeEnum.valueOf(type)) {
			case DIRECT:
				binding = BindingBuilder.bind(createQueue(bindingSingle.getQueueName()))
						.to(new DirectExchange(bindingSingle.getExchangeName()))
						.with(bindingSingle.getRoutingKey());
				break;
			case TOPIC:
				binding = BindingBuilder.bind(createQueue(bindingSingle.getQueueName()))
						.to(new TopicExchange(bindingSingle.getExchangeName()))
						.with(bindingSingle.getRoutingKey());
				break;
			case HEADERS:
				binding = BindingBuilder.bind(createQueue(bindingSingle.getQueueName()))
						.to(new HeadersExchange(bindingSingle.getExchangeName()))
						.whereAll(exchangeHeaderToMap(bindingSingle.getRoutingKey()))
						.match();
				break;
			case FANOUT:
				binding = BindingBuilder.bind(createQueue(bindingSingle.getQueueName()))
						.to(new FanoutExchange(bindingSingle.getExchangeName()));
				break;
			default: {
				binding = BindingBuilder.bind(createQueue(bindingSingle.getQueueName()))
						.to(createExchange(bindingSingle.getExchangeName(), bindingSingle.getExchangeType()))
						.with(bindingSingle.getRoutingKey()).noargs();
			}
		}
		return binding;
	}

	private Map<String, Object> exchangeHeaderToMap(String key) {
		String[] mapEntrys = key.split(";");
		Map<String, Object> result = new HashMap<>(mapEntrys.length);
		for (String mapEntry : mapEntrys) {
			String[] entry = mapEntry.split("=");
			result.put(entry[0], entry[1]);
		}
		return result;
	}


	private List<Exchange> buildExchanges(List<BindingParameter> BindingParameter) {
		List<Exchange> exchanges = new ArrayList<>(BindingParameter.size());
		for (BindingParameter exchange : BindingParameter) {
			exchanges.add(createExchange(exchange.getExchangeName(), exchange.getExchangeType()));
		}
		return exchanges;
	}

	/**
	 * 构造交换器对象,默认持久化
	 *
	 * @param name name
	 * @return Exchange
	 */
	private Exchange createExchange(String name, String type) {
		return transferExchanges(name, type);
	}


	private Exchange transferExchanges(String name, String type) {
		Assert.hasText(type, "ExchangeType must not be empty");
		String typeName = type.toUpperCase();
		Exchange exchange;
		switch (ExchangeEnum.valueOf(typeName)) {
			case DIRECT:
				exchange = new DirectExchange(name);
				break;
			case TOPIC:
				exchange = new TopicExchange(name);
				break;
			case HEADERS:
				exchange = new HeadersExchange(name);
				break;
			case FANOUT:
				exchange = new FanoutExchange(name);
				break;
			default: {
				exchange = new CustomExchange(name, type, true, false);
			}
		}
		return exchange;
	}

}
