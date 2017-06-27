package org.throwable.rabbitmq.annotation;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.throwable.rabbitmq.configuration.RabbitmqProperties;
import org.throwable.rabbitmq.configuration.SlimeRabbitBootstrapConfiguration;
import org.throwable.rabbitmq.support.RabbitInstanceRegistrar;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 16:56
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {RabbitInstanceRegistrar.class, SlimeRabbitBootstrapConfiguration.class})
@EnableConfigurationProperties(RabbitmqProperties.class)
public @interface EnableAmqpRabbitmq {
}
