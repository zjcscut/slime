package org.throwable.rabbitmq.support;

import jodd.util.StringUtil;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.throwable.rabbitmq.common.RebbitmqConstants;
import org.throwable.rabbitmq.configuration.*;
import org.throwable.rabbitmq.exception.RabbitmqRegisterException;
import org.throwable.utils.JacksonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 16:53
 */
public class RabbitmqInstanceRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private static final AcknowledgeMode DEFAULT_ACKNOWLEDGEMODE = AcknowledgeMode.AUTO;

    private static final Integer DEFAULT_CONCURRENTCONSUMERS = 1;

    private static final Integer DEFAULT_MAX_CONCURRENTCONSUMERS = 20;

    private static final String DEFAULT_VIRTUALHOST = "/";

    private String location;


    @Override
    public void setEnvironment(Environment environment) {
        this.location = environment.getProperty(RabbitmqProperties.PREFIX + ".location");
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) registry;
        initAndRegisterRabbitmqProperties(beanFactory);
        registerMultiInstanceRabbitmqComponent(beanFactory);
    }

    private void initAndRegisterRabbitmqProperties(BeanFactory beanFactory) {
        Assert.hasText(location, "Amqp rabbitmq configuration location must not be empty");
        SlimeRabbitmqProperties sp = JacksonUtils.parseFromJsonFile(location, SlimeRabbitmqProperties.class);
        ((ConfigurableBeanFactory) beanFactory).registerSingleton(sp.getClass().getCanonicalName(), sp);
        RabbitmqRegistrarPropertiesDelegate.parseSlimeRabbitmqProperties(sp);
    }

    private void registerMultiInstanceRabbitmqComponent(ConfigurableBeanFactory beanFactory) {
        Map<String, InstanceHolder> instances = RabbitmqRegistrarPropertiesManager.getAllInstances();
        for (Map.Entry<String, InstanceHolder> entry : instances.entrySet()) {
            if (null != RabbitmqRegistrarPropertiesManager.getProducerInstance(entry.getKey())) {
                registerProducerConnectionFactory(entry.getValue(), beanFactory);
            } else {
                registerConsumerConnectionFactory(entry.getValue(), beanFactory);
            }
        }
        registerRabbitAdmin(beanFactory);
        registerRabbitListener(beanFactory);
        registerRoutingConnectionFactory(beanFactory);
        registerDefaultRabbitTemplate(beanFactory);
    }

    private void registerProducerConnectionFactory(InstanceHolder holder, ConfigurableBeanFactory beanFactory) {
        CachingConnectionFactory factory = createConnectionFactory(holder);
        RabbitmqProducerInstanceProperties rpip = (RabbitmqProducerInstanceProperties) holder.getInstance();
        factory.setPublisherReturns(rpip.getUseReturnCallback());
        factory.setPublisherConfirms(rpip.getUseConfirmCallback());
        String factoryName = RebbitmqConstants.RABBIT_CONNECTION_FACTORY_NAME_PREFIX + holder.getinstanceSignature();
        beanFactory.registerSingleton(factoryName, factory);
        RabbitmqRegistrarPropertiesDelegate.cacheRegisteredConnectionFactory(holder.getinstanceSignature(), factoryName);
    }

    private void registerConsumerConnectionFactory(InstanceHolder holder, ConfigurableBeanFactory beanFactory) {
        String factoryName = RebbitmqConstants.RABBIT_CONNECTION_FACTORY_NAME_PREFIX + holder.getinstanceSignature();
        beanFactory.registerSingleton(factoryName, createConnectionFactory(holder));
        RabbitmqRegistrarPropertiesDelegate.cacheRegisteredConnectionFactory(holder.getinstanceSignature(), factoryName);
    }

    private CachingConnectionFactory createConnectionFactory(InstanceHolder holder) {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(holder.getInstance().getHost());
        factory.setPort(holder.getInstance().getPort());
        factory.setUsername(holder.getInstance().getUsername());
        factory.setPassword(holder.getInstance().getPassword());
        String virtualHost = holder.getInstance().getVirtualHost();
        if (StringUtil.isNotBlank(virtualHost)) {
            factory.setVirtualHost(virtualHost);
        } else {
            factory.setVirtualHost(DEFAULT_VIRTUALHOST);
        }
        return factory;
    }

    private void registerRabbitAdmin(ConfigurableBeanFactory beanFactory) {
        Map<Object, String> factors = RabbitmqRegistrarPropertiesDelegate.getConnectionFactoryNames();
        for (Map.Entry<Object, String> entry : factors.entrySet()) {
            String sign = (String) entry.getKey();
            String rabbitAdminName = RebbitmqConstants.RABBITADMIN_NAME_PREFIX + sign;
            RabbitAdmin rabbitAdmin = createRabbitAdmin(beanFactory.getBean(entry.getValue(), CachingConnectionFactory.class));
            beanFactory.registerSingleton(rabbitAdminName, rabbitAdmin);
        }
        initRabbitmqComponent(beanFactory);
    }

    private void initRabbitmqComponent(ConfigurableBeanFactory beanFactory) {
        Map<String, List<BindingParameter>> bindingParameters = RabbitmqRegistrarPropertiesManager.getAllProducerBindingParameters();
        for (Map.Entry<String, List<BindingParameter>> entry : bindingParameters.entrySet()) {
            String rabbitAdminName = RebbitmqConstants.RABBITADMIN_NAME_PREFIX + entry.getKey();
            RabbitAdmin rabbitAdmin = beanFactory.getBean(rabbitAdminName, RabbitAdmin.class);
            RabbitAdminOperator operator = new RabbitAdminOperator(rabbitAdmin);
            operator.init(entry.getValue());
        }
        Map<String, List<ConsumerBindingParameter>> consumerBindingParameters = RabbitmqRegistrarPropertiesManager.getAllConsumerBindingParameters();
        for (Map.Entry<String, List<ConsumerBindingParameter>> entry : consumerBindingParameters.entrySet()) {
            String rabbitAdminName = RebbitmqConstants.RABBITADMIN_NAME_PREFIX + entry.getKey();
            RabbitAdmin rabbitAdmin = beanFactory.getBean(rabbitAdminName, RabbitAdmin.class);
            RabbitAdminOperator operator = new RabbitAdminOperator(rabbitAdmin);
            operator.init(RabbitmqRegistrarPropertiesManager.parseBindingParameter(entry.getValue()));
        }
    }

    private RabbitAdmin createRabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    private void registerRabbitListener(ConfigurableBeanFactory beanFactory) {
        Map<String, List<ConsumerBindingParameter>> bindingParameters
                = RabbitmqRegistrarPropertiesManager.getAllConsumerBindingParameters();
        for (Map.Entry<String, List<ConsumerBindingParameter>> entry : bindingParameters.entrySet()) {
            String instanceSignature = entry.getKey();
            CachingConnectionFactory factory
                    = beanFactory.getBean(RebbitmqConstants.RABBIT_CONNECTION_FACTORY_NAME_PREFIX + instanceSignature,
                    CachingConnectionFactory.class);
            List<ConsumerBindingParameter> parameters = entry.getValue();
            for (ConsumerBindingParameter parameter : parameters) {
                String listenerClassName = parameter.getListenerClassName();
                Assert.hasText(listenerClassName, "Rabbitmq listener class name must not be empty." + parameter);
                SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(factory);
                container.setMessageConverter(contentTypeDelegatingMessageConverter());
                if (null != parameter.getConcurrentConsumers()) {
                    container.setConcurrentConsumers(parameter.getConcurrentConsumers());
                } else {
                    container.setConcurrentConsumers(DEFAULT_CONCURRENTCONSUMERS);
                }
                if (null != parameter.getMaxConcurrentConsumers()) {
                    container.setMaxConcurrentConsumers(parameter.getMaxConcurrentConsumers());
                } else {
                    container.setMaxConcurrentConsumers(DEFAULT_MAX_CONCURRENTCONSUMERS);
                }
                if (StringUtil.isNotBlank(parameter.getAcknowledgeMode())) {
                    container.setAcknowledgeMode(AcknowledgeMode.valueOf(parameter.getAcknowledgeMode().toUpperCase()));
                } else {
                    container.setAcknowledgeMode(DEFAULT_ACKNOWLEDGEMODE);
                }
                container.setQueueNames(parameter.getQueueName());
                try {
                    Class<?> listenerClass = Class.forName(listenerClassName);
                    Object listenerBean = beanFactory.getBean(listenerClass);
                    container.setMessageListener(listenerBean);
                } catch (Exception e) {
                    throw new RabbitmqRegisterException(e);
                }
                beanFactory.registerSingleton(
                        RebbitmqConstants.RABBIT_MESSAGE_LISTENER_CONTAINER_NAME_PREFIX + instanceSignature
                                + RebbitmqConstants.DEFAULT_NAMEKEY_SUFFIX + resolveQueueName(parameter.getQueueName()),
                        container
                );
            }
        }

    }

    private void registerRoutingConnectionFactory(ConfigurableBeanFactory beanFactory) {
        Map<Object, ConnectionFactory> factoryMap = new HashMap<>();
        CachingConnectionFactory defaultFactory = null;
        int defaultIndex = 0;
        for (Map.Entry<Object, String> entry : RabbitmqRegistrarPropertiesDelegate.getConnectionFactoryNames().entrySet()) {
            CachingConnectionFactory factory = beanFactory.getBean(entry.getValue(), CachingConnectionFactory.class);
            factoryMap.put(entry.getKey(), factory);
            if (0 == defaultIndex) {
                defaultFactory = factory;
            }
            defaultIndex++;
        }
        SimpleRoutingConnectionFactory routingConnectionFactory = new SimpleRoutingConnectionFactory();
        routingConnectionFactory.setTargetConnectionFactories(factoryMap);
        routingConnectionFactory.setDefaultTargetConnectionFactory(defaultFactory);
        beanFactory.registerSingleton(RebbitmqConstants.ROUTING_RABBIT_CONNECTION_FACTORY_NAME_PREFIX, routingConnectionFactory);

        //为了避免多ConnectionFactory实例导致默认的rabbitListenerContainerFactory创建异常,实际上没有作用
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        containerFactory.setConnectionFactory(routingConnectionFactory);
        //为了避免多RabbitListenerContainerFactory实例导致RabbitAnnotationDrivenConfiguration#rabbitListenerContainerFactory注入失败
        beanFactory.registerSingleton("rabbitListenerContainerFactory", routingConnectionFactory);
    }

    private void registerDefaultRabbitTemplate(ConfigurableBeanFactory beanFactory) {
        SimpleRoutingConnectionFactory routingConnectionFactory = beanFactory.getBean(
                RebbitmqConstants.ROUTING_RABBIT_CONNECTION_FACTORY_NAME_PREFIX,
                SimpleRoutingConnectionFactory.class
        );
        RabbitTemplate rabbitTemplate = new RabbitTemplate(routingConnectionFactory);
        rabbitTemplate.setEncoding(RebbitmqConstants.DEFAULT_CHARSET_ENCODING);
        rabbitTemplate.setMessageConverter(contentTypeDelegatingMessageConverter());
        beanFactory.registerSingleton(RebbitmqConstants.RABBIT_TEMPLATE_NAME_PREFIX, rabbitTemplate);
    }

    private ContentTypeDelegatingMessageConverter contentTypeDelegatingMessageConverter() {
        ContentTypeDelegatingMessageConverter converter = new ContentTypeDelegatingMessageConverter();
        converter.addDelegate(MediaType.APPLICATION_JSON_VALUE, new Jackson2JsonMessageConverter());
        converter.addDelegate(MediaType.TEXT_PLAIN_VALUE, new Jackson2JsonMessageConverter());
        return converter;
    }

    private String resolveQueueName(String originQueueName) {
        return "[" + originQueueName + "]";
    }

}
