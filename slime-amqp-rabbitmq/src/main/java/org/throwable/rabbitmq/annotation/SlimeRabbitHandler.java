package org.throwable.rabbitmq.annotation;

import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/30 11:20
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@MessageMapping
@Documented
public @interface SlimeRabbitHandler {
}
