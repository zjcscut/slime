package org.throwable.rabbitmq.support;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/23 10:54
 */
public class MultiInstanceRabbitTemplateAdapter extends AbstractMultiInstanceRedisTemplate implements MultiInstanceRabbitTemplateSupports {

    private ConfigurableBeanFactory beanFactory;

    private static final Map<String, String> templateNamesHolder = new ConcurrentHashMap<>();

    public void setBeanFactory(BeanFactory beanFactory)  {
        this.beanFactory = (ConfigurableBeanFactory) beanFactory;
    }

    @Override
    public void multiSend(String exchange, String routingKey, Message message, String instanceSignature) throws AmqpException {
        beanFactory.getBean(templateNamesHolder.get(instanceSignature), RabbitTemplate.class).send(exchange, routingKey, message);
    }

    @Override
    public void multiSend(String exchange, String routingKey, Message message, CorrelationData correlationData, String instanceSignature) throws AmqpException {
        beanFactory.getBean(templateNamesHolder.get(instanceSignature), RabbitTemplate.class).send(exchange, routingKey, message, correlationData);
    }

    @Override
    public void multiSendJson(String exchange, String routingKey, Object message, String instanceSignature) throws AmqpException {
        beanFactory.getBean(templateNamesHolder.get(instanceSignature), RabbitTemplate.class).send(exchange, routingKey, convertObjectToJsonMessage(message));
    }

    @Override
    public void multiSendJson(String exchange, String routingKey, Object message, CorrelationData correlationData, String instanceSignature) throws AmqpException {
        beanFactory.getBean(templateNamesHolder.get(instanceSignature), RabbitTemplate.class).send(exchange, routingKey, convertObjectToJsonMessage(message), correlationData);
    }

    public void addNameHolderPair(String instanceSignature,String templateName){
		templateNamesHolder.put(instanceSignature,templateName);
	}

}
