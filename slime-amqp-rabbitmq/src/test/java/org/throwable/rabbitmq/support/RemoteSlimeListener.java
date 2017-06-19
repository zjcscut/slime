package org.throwable.rabbitmq.support;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.stereotype.Component;
import org.throwable.rabbitmq.annotation.SlimeRabbitListener;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/12 11:21
 */
//@Component
public class RemoteSlimeListener {

    @SlimeRabbitListener(instanceSignature = "REMOTE", bindings =
    @QueueBinding(
            value = @Queue(value = "queue-1", durable = "true"),
            exchange = @Exchange(value = "exchange1", durable = "true")
    ))
    public void onMessage(Message message) {
        System.out.println("RemoteSlimeListener receive message --> " + new String(message.getBody()));
    }
}
