package org.throwable.rabbitmq.support;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/23 15:48
 */
public class RabbitConfirmCallbackListener implements RabbitTemplate.ConfirmCallback {

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
    }
}
