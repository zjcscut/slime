package org.throwable.rabbitmq.configuration;

import org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.throwable.rabbitmq.support.SlimeRabbitmqListenerAnnotationProcessor;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/12 11:43
 */
@Configuration
public class SlimeRabbitBootstrapConfiguration {

    private static final String BEAN_NAME = "org.throwable.rabbitmq.support.SlimeRabbitmqListenerAnnotationProcessor";

    @Bean(name = BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public SlimeRabbitmqListenerAnnotationProcessor slimeRabbitmqListenerAnnotationProcessor(){
        return new SlimeRabbitmqListenerAnnotationProcessor();
    }

    @Bean(name = RabbitListenerConfigUtils.RABBIT_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME)
    public RabbitListenerEndpointRegistry defaultRabbitListenerEndpointRegistry() {
        return new RabbitListenerEndpointRegistry();
    }
}
