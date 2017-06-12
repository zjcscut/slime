package org.throwable.rabbitmq.annotation;

import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/28 12:06
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Documented
@MessageMapping
@Repeatable(value = SlimeRabbitListeners.class)
public @interface SlimeRabbitListener {

	/**
	 * mq instance signature
	 */
	String instanceSignature();

	/**
	 * queue name array,if this field is defined,you must not to define bindings
	 */
	String[] queues() default {};

	/**
	 *  initial number of consumer
	 */
	int concurrentConsumers() default 1;

	/**
	 *  max number of consumer
	 */
	int maxConcurrentConsumers() default 10;

	/**
	 * rabbitmq priority
	 */
	String priority() default "";

	/**
	 * binding parameters
	 * @see org.springframework.amqp.rabbit.annotation.RabbitListener
	 */
	QueueBinding[] bindings() default {};

}
