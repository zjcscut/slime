package org.throwable.rabbitmq.support;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/23 15:49
 */
public class RabbitReturnCallbackListener implements RabbitTemplate.ReturnCallback{

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {

    }
}
