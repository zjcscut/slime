package org.throwable.rabbitmq.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.*;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.*;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.throwable.rabbitmq.annotation.SlimeRabbitHandler;
import org.throwable.rabbitmq.annotation.SlimeRabbitListener;
import org.throwable.rabbitmq.annotation.SlimeRabbitListeners;
import org.throwable.rabbitmq.common.RebbitmqConstants;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author throwable
 * @version v1.0
 * @description refer to {@link org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor}
 * @since 2017/5/30 3:33
 */
@Slf4j
public class SlimeRabbitmqListenerAnnotationProcessor
        implements BeanFactoryAware, BeanPostProcessor, Ordered, SmartInitializingSingleton {

    private RabbitListenerEndpointRegistry endpointRegistry;

    private final RabbitHandlerMethodFactoryAdapter messageHandlerMethodFactory =
            new RabbitHandlerMethodFactoryAdapter();

    private final RabbitListenerEndpointRegistrar registrar = new RabbitListenerEndpointRegistrar();

    private static final ConversionService CONVERSION_SERVICE = new DefaultConversionService();

    private final ConcurrentMap<Class<?>, TypeMetadata> typeCache = new ConcurrentHashMap<>();

    private BeanExpressionResolver resolver = new StandardBeanExpressionResolver();

    private BeanExpressionContext expressionContext;

    private final AtomicInteger counter = new AtomicInteger();

    private BeanFactory beanFactory;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        if (beanFactory instanceof ConfigurableListableBeanFactory) {
            this.resolver = ((ConfigurableListableBeanFactory) beanFactory).getBeanExpressionResolver();
            this.expressionContext = new BeanExpressionContext((ConfigurableListableBeanFactory) beanFactory, null);
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        TypeMetadata typeMetadata = typeCache.get(targetClass);
        if (null == typeMetadata) {
            typeMetadata = buildListenersAnnotationMetadata(targetClass);
            this.typeCache.putIfAbsent(targetClass, typeMetadata);
        }
        for (ListenerMethod lm : typeMetadata.listenerMethods) {  //use only @SlimeRabbitListener
            for (SlimeRabbitListener slimeRabbitListener : lm.annotations) {
                processAmqpListeners(slimeRabbitListener, lm.method, bean, beanName);
            }
        }
        if (typeMetadata.handlerMethods.length > 0) { //use class level @SlimeRabbitListener and @SlimeRabbitHandler
            processMultiMethodListerners(typeMetadata.classAnnotations, typeMetadata.handlerMethods, bean, beanName);
        }
        return bean;
    }

    @Override
    public void afterSingletonsInstantiated() {

    }

    /**
     * process listeners use only @SlimeRabbitListener
     */
    protected void processAmqpListeners(SlimeRabbitListener slimeRabbitListener, Method method, Object bean, String beanName) {
        Method methodToUse = checkMethodProxyAndReturn(method, bean);
        MethodRabbitListenerEndpoint endpoint = new MethodRabbitListenerEndpoint();
        endpoint.setMethod(methodToUse);
        endpoint.setBeanFactory(this.beanFactory);
        processListener(endpoint, slimeRabbitListener, bean, methodToUse, beanName);
    }

    /**
     * process listeners use class level @SlimeRabbitListener and @SlimeRabbitHandler
     */
    protected void processMultiMethodListerners(SlimeRabbitListener[] slimeRabbitListeners, Method[] multiMethods,
                                                Object bean, String beanName) {
        List<Method> checkedMethods = new ArrayList<>(multiMethods.length);
        for (Method method : multiMethods) {
            checkedMethods.add(checkMethodProxyAndReturn(method, bean));
        }
        for (SlimeRabbitListener classLevelListener : slimeRabbitListeners) {
            MultiMethodRabbitListenerEndpoint endpoint = new MultiMethodRabbitListenerEndpoint(checkedMethods, bean);
            endpoint.setBeanFactory(this.beanFactory);
            processListener(endpoint, classLevelListener, bean, bean.getClass(), beanName);
        }
    }

    private void processListener(MethodRabbitListenerEndpoint endpoint, SlimeRabbitListener slimeRabbitListener, Object bean,
                                 Object target, String beanName) {
        String instanceSign = slimeRabbitListener.instanceSign();
        Assert.hasText(instanceSign, String.format("Listener [%s] instanceSign must not be empty!", beanName));
        InstanceHolder instanceHolder = RabbitmqRegistrarPropertiesManager.getConsumerInstance(instanceSign);
        Assert.notNull(instanceHolder, String.format("Rabbitmq instance of Listener [%s] must not be null!", beanName));
        endpoint.setBean(bean);
        endpoint.setMessageHandlerMethodFactory(this.messageHandlerMethodFactory);
        endpoint.setId(resolveEndpointId(slimeRabbitListener));
        endpoint.setQueueNames(resolveListenerQueues(slimeRabbitListener));
        String priority = resolve(slimeRabbitListener.priority());
        if (StringUtils.hasText(priority)) {
            try {
                endpoint.setPriority(Integer.valueOf(priority));
            } catch (NumberFormatException ex) {
                throw new BeanInitializationException("Invalid priority value for " +
                        target + " (must be an integer)", ex);
            }
        }
        ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) this.beanFactory;
        endpoint.setAdmin(beanFactory.getBean(RebbitmqConstants.RABBITADMIN_NAME_PREFIX + instanceSign, RabbitAdmin.class));
        if (!beanFactory.containsSingleton(RebbitmqConstants.RABBIT_MESSAGE_LISTENER_CONTAINER_FACTORY_NAME + instanceSign
                + RebbitmqConstants.DEFAULT_NAMEKEY_SUFFIX + endpoint.getQueueNames())) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Rabbitmq listener [%s] for queue %s has been registered!", beanName, endpoint.getQueueNames()));
            }
        } else {
            SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
            CachingConnectionFactory connectionFactory
                    = beanFactory.getBean(RebbitmqConstants.RABBIT_CONNECTION_FACTORY_NAME_PREFIX + instanceSign,
                    CachingConnectionFactory.class);
            containerFactory.setConnectionFactory(connectionFactory);
            containerFactory.setAcknowledgeMode(AcknowledgeMode.AUTO);  //默认自动ack
            containerFactory.setConcurrentConsumers(slimeRabbitListener.concurrentConsumers());
            containerFactory.setMaxConcurrentConsumers(slimeRabbitListener.maxConcurrentConsumers());

        }
    }

    private String resolveEndpointId(SlimeRabbitListener slimeRabbitListener) {
        return "org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#" + this.counter.getAndIncrement();
    }

    private String[] resolveListenerQueues(SlimeRabbitListener slimeRabbitListener) {
        String[] queues = slimeRabbitListener.queues();
        QueueBinding[] bindings = slimeRabbitListener.bindings();
        if (queues.length > 0 && bindings.length > 0) {
            throw new BeanInitializationException("@SlimeRabbitListener can have 'queues' or 'bindings' but not both");
        }
        List<String> result = new ArrayList<>();
        if (queues.length > 0) {

        } else {

        }
        return result.toArray(new String[result.size()]);
    }

    private TypeMetadata buildListenersAnnotationMetadata(Class<?> targetClass) {
        Collection<SlimeRabbitListener> classLevelListeners = findListenerAnnotations(targetClass);
        final boolean hasClassLevelListeners = classLevelListeners.size() > 0;
        final List<ListenerMethod> methods = new ArrayList<>();
        final List<Method> multiMethods = new ArrayList<>();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Collection<SlimeRabbitListener> listenerAnnotations = SlimeRabbitmqListenerAnnotationProcessor.this.findListenerAnnotations(method);
            if (listenerAnnotations.size() > 0) {
                methods.add(new ListenerMethod(method,
                        listenerAnnotations.toArray(new SlimeRabbitListener[listenerAnnotations.size()])));
            }
            if (hasClassLevelListeners) {
                SlimeRabbitHandler slimeRabbitHandler = AnnotationUtils.findAnnotation(method, SlimeRabbitHandler.class);
                if (null != slimeRabbitHandler) {
                    multiMethods.add(method);
                }
            }

        }, ReflectionUtils.USER_DECLARED_METHODS);
        if (methods.isEmpty() && multiMethods.isEmpty()) {
            return TypeMetadata.EMPTY;
        }
        return new TypeMetadata(
                methods.toArray(new ListenerMethod[methods.size()]),
                multiMethods.toArray(new Method[multiMethods.size()]),
                classLevelListeners.toArray(new SlimeRabbitListener[classLevelListeners.size()]));
    }

    /*
     * AnnotationUtils.getRepeatableAnnotations does not look at interfaces
     */
    private Collection<SlimeRabbitListener> findListenerAnnotations(Class<?> clazz) {
        Set<SlimeRabbitListener> listeners = new HashSet<>();
        SlimeRabbitListener ann = AnnotationUtils.findAnnotation(clazz, SlimeRabbitListener.class);
        if (ann != null) {
            listeners.add(ann);
        }
        SlimeRabbitListeners anns = AnnotationUtils.findAnnotation(clazz, SlimeRabbitListeners.class);
        if (anns != null) {
            Collections.addAll(listeners, anns.value());
        }
        return listeners;
    }

    /*
     * AnnotationUtils.getRepeatableAnnotations does not look at interfaces
     */
    private Collection<SlimeRabbitListener> findListenerAnnotations(Method method) {
        Set<SlimeRabbitListener> listeners = new HashSet<>();
        SlimeRabbitListener ann = AnnotationUtils.findAnnotation(method, SlimeRabbitListener.class);
        if (ann != null) {
            listeners.add(ann);
        }
        SlimeRabbitListeners anns = AnnotationUtils.findAnnotation(method, SlimeRabbitListeners.class);
        if (anns != null) {
            Collections.addAll(listeners, anns.value());
        }
        return listeners;
    }

    private Method checkMethodProxyAndReturn(Method method, Object bean) {
        if (AopUtils.isJdkDynamicProxy(bean)) {
            try {
                // Found a @RabbitListener method on the target class for this JDK proxy ->
                // is it also present on the proxy itself?
                method = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
                Class<?>[] proxiedInterfaces = ((Advised) bean).getProxiedInterfaces();
                for (Class<?> iface : proxiedInterfaces) {
                    try {
                        method = iface.getMethod(method.getName(), method.getParameterTypes());
                        break;
                    } catch (NoSuchMethodException noMethod) {
                        //ignore
                    }
                }
            } catch (SecurityException ex) {
                ReflectionUtils.handleReflectionException(ex);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException(String.format(
                        "@RabbitListener method '%s' found on bean target class '%s', " +
                                "but not found in any interface(s) for bean JDK proxy. Either " +
                                "pull the method up to an interface or switch to subclass (CGLIB) " +
                                "proxies by setting proxy-target-class/proxyTargetClass " +
                                "attribute to 'true'", method.getName(), method.getDeclaringClass().getSimpleName()));
            }
        }
        return method;
    }

    /**
     * The metadata holder of the class with {@link SlimeRabbitListener}
     * and {@link SlimeRabbitHandler} annotations.
     */
    private static class TypeMetadata {

        /**
         * Methods annotated with {@link SlimeRabbitListener}.
         */
        final ListenerMethod[] listenerMethods;

        /**
         * Methods annotated with {@link SlimeRabbitHandler}.
         */
        final Method[] handlerMethods;

        /**
         * Class level {@link SlimeRabbitListener} annotations.
         */
        final SlimeRabbitListener[] classAnnotations;

        static final TypeMetadata EMPTY = new TypeMetadata();

        private TypeMetadata() {
            this.listenerMethods = new ListenerMethod[0];
            this.handlerMethods = new Method[0];
            this.classAnnotations = new SlimeRabbitListener[0];
        }

        TypeMetadata(ListenerMethod[] methods, Method[] multiMethods, SlimeRabbitListener[] classLevelListeners) {
            this.listenerMethods = methods;
            this.handlerMethods = multiMethods;
            this.classAnnotations = classLevelListeners;
        }
    }

    /**
     * A method annotated with {@link SlimeRabbitListener}, together with the annotations.
     */
    private static class ListenerMethod {

        final Method method;

        final SlimeRabbitListener[] annotations;

        ListenerMethod(Method method, SlimeRabbitListener[] annotations) {
            this.method = method;
            this.annotations = annotations;
        }
    }

    private class RabbitHandlerMethodFactoryAdapter implements MessageHandlerMethodFactory {

        private MessageHandlerMethodFactory messageHandlerMethodFactory;

        public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory rabbitHandlerMethodFactory1) {
            this.messageHandlerMethodFactory = rabbitHandlerMethodFactory1;
        }

        @Override
        public InvocableHandlerMethod createInvocableHandlerMethod(Object bean, Method method) {
            return getMessageHandlerMethodFactory().createInvocableHandlerMethod(bean, method);
        }

        private MessageHandlerMethodFactory getMessageHandlerMethodFactory() {
            if (this.messageHandlerMethodFactory == null) {
                this.messageHandlerMethodFactory = createDefaultMessageHandlerMethodFactory();
            }
            return this.messageHandlerMethodFactory;
        }

        private MessageHandlerMethodFactory createDefaultMessageHandlerMethodFactory() {
            DefaultMessageHandlerMethodFactory defaultFactory = new DefaultMessageHandlerMethodFactory();
            defaultFactory.setBeanFactory(SlimeRabbitmqListenerAnnotationProcessor.this.beanFactory);
            defaultFactory.afterPropertiesSet();
            return defaultFactory;
        }

    }

    /**
     * Resolve the specified value if possible.
     *
     * @see ConfigurableBeanFactory#resolveEmbeddedValue
     */
    private String resolve(String value) {
        if (this.beanFactory != null && this.beanFactory instanceof ConfigurableBeanFactory) {
            return ((ConfigurableBeanFactory) this.beanFactory).resolveEmbeddedValue(value);
        }
        return value;
    }

    private ContentTypeDelegatingMessageConverter contentTypeDelegatingMessageConverter() {
        ContentTypeDelegatingMessageConverter converter = new ContentTypeDelegatingMessageConverter();
        converter.addDelegate(MediaType.APPLICATION_JSON_VALUE, new Jackson2JsonMessageConverter());
        converter.addDelegate(MediaType.TEXT_PLAIN_VALUE, new Jackson2JsonMessageConverter());
        return converter;
    }
}
