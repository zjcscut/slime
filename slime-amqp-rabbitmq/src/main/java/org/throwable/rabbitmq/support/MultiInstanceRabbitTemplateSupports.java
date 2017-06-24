package org.throwable.rabbitmq.support;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.support.CorrelationData;

/**
 * @author throwable
 * @version v1.0
 * @description 多实例mq消息发送转换支持接口
 * @since 2017/6/23 10:54
 */
public interface MultiInstanceRabbitTemplateSupports {

    void multiSend(String exchange, String routingKey, final Message message, String instanceSignature) throws AmqpException;

    void multiSend(String exchange, String routingKey, final Message message, CorrelationData correlationData, String instanceSignature) throws AmqpException;

    void multiSendJson(String exchange, String routingKey, final Object message, String instanceSignature) throws AmqpException;

    void multiSendJson(String exchange, String routingKey, final Object message, CorrelationData correlationData, String instanceSignature) throws AmqpException;
}
