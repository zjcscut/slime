package org.throwable.rabbitmq.annotation;

import org.springframework.amqp.rabbit.annotation.QueueBinding;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/26 9:40
 */
@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = SlimeRabbitListeners.class)
public @interface SlimeRabbitListener {

    String instanceSign();

    QueueBinding[] bindings() default {};

    int concurrentConsumers() default 1;

    int maxConcurrentConsumers() default 10;

}
