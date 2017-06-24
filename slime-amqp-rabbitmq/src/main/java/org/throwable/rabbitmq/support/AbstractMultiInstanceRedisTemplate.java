package org.throwable.rabbitmq.support;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/23 14:41
 */
public abstract class AbstractMultiInstanceRedisTemplate {

    private final Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

    protected Message convertObjectToJsonMessage(final Object target){
        MessageProperties messageProperties =  new MessageProperties();
        messageProperties.setContentEncoding("UTF-8");
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        return converter.toMessage(target,messageProperties);
    }

}
