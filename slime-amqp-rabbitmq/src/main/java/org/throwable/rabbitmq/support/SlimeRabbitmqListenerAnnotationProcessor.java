package org.throwable.rabbitmq.support;

import jodd.util.ArraysUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.Argument;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.MethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.MultiMethodRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
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
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.throwable.rabbitmq.annotation.SlimeRabbitHandler;
import org.throwable.rabbitmq.annotation.SlimeRabbitListener;
import org.throwable.rabbitmq.annotation.SlimeRabbitListeners;
import org.throwable.rabbitmq.common.RebbitmqConstants;
import org.throwable.rabbitmq.configuration.ConsumerBindingParameter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor.DEFAULT_RABBIT_LISTENER_CONTAINER_FACTORY_BEAN_NAME;

/**
 * @author throwable
 * @version v1.0
 * @description refer to {@link org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor}
 * @since 2017/5/30 3:33
 */
@Slf4j
public class SlimeRabbitmqListenerAnnotationProcessor
        implements BeanFactoryAware, BeanClassLoaderAware, BeanPostProcessor, Ordered, SmartInitializingSingleton {

    private String containerFactoryBeanName = DEFAULT_RABBIT_LISTENER_CONTAINER_FACTORY_BEAN_NAME;

    private RabbitListenerEndpointRegistry endpointRegistry;

    private final RabbitHandlerMethodFactoryAdapter messageHandlerMethodFactory =
            new RabbitHandlerMethodFactoryAdapter();

    private final RabbitListenerEndpointRegistrar registrar = new RabbitListenerEndpointRegistrar();

    private static final ConversionService CONVERSION_SERVICE = new DefaultConversionService();

    private final ConcurrentMap<Class<?>, TypeMetadata> typeCache = new ConcurrentHashMap<>();

    private final Set<String> emptyStringArguments = new HashSet<>();

    private BeanExpressionResolver resolver = new StandardBeanExpressionResolver();

    private BeanExpressionContext expressionContext;

    private final AtomicInteger counter = new AtomicInteger();

    private BeanFactory beanFactory;

    private ClassLoader beanClassLoader;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
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
        this.registrar.setBeanFactory(this.beanFactory);

        if (this.beanFactory instanceof ListableBeanFactory) {
            Map<String, RabbitListenerConfigurer> instances =
                    ((ListableBeanFactory) this.beanFactory).getBeansOfType(RabbitListenerConfigurer.class);
            for (RabbitListenerConfigurer configurer : instances.values()) {
                configurer.configureRabbitListeners(this.registrar);
            }
        }

        if (this.registrar.getEndpointRegistry() == null) {
            if (this.endpointRegistry == null) {
                Assert.state(this.beanFactory != null,
                        "BeanFactory must be set to find endpoint registry by bean name");
                this.endpointRegistry = this.beanFactory.getBean(
                        RabbitListenerConfigUtils.RABBIT_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME,
                        RabbitListenerEndpointRegistry.class);
            }
            this.registrar.setEndpointRegistry(this.endpointRegistry);
        }

        if (this.containerFactoryBeanName != null) {
            this.registrar.setContainerFactoryBeanName(this.containerFactoryBeanName);
        }

        // Set the custom handler method factory once resolved by the configurer
        MessageHandlerMethodFactory handlerMethodFactory = this.registrar.getMessageHandlerMethodFactory();
        if (handlerMethodFactory != null) {
            this.messageHandlerMethodFactory.setMessageHandlerMethodFactory(handlerMethodFactory);
        }

        // Actually register all listeners
        this.registrar.afterPropertiesSet();

        // clear the cache - prototype beans will be re-cached.
        this.typeCache.clear();
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
        String instanceSignature = slimeRabbitListener.instanceSignature();
        Assert.hasText(instanceSignature, String.format("Listener [%s] instanceSignature must not be empty!", beanName));
        InstanceHolder instanceHolder = RabbitmqRegistrarPropertiesManager.getConsumerInstance(instanceSignature);
        Assert.notNull(instanceHolder, String.format("Rabbitmq instance of Listener [%s] must not be null!", beanName));
        endpoint.setBean(bean);
        endpoint.setMessageHandlerMethodFactory(this.messageHandlerMethodFactory);
        endpoint.setId(resolveEndpointId(slimeRabbitListener));
        String priority = resolve(slimeRabbitListener.priority());
        if (StringUtils.hasText(priority)) {
            try {
                endpoint.setPriority(Integer.valueOf(priority));
            } catch (NumberFormatException ex) {
                throw new BeanInitializationException("Invalid priority value for " + target + " (must be an integer)", ex);
            }
        }
        ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) this.beanFactory;
        SimpleRabbitListenerContainerFactory containerFactory;
        endpoint.setAdmin(beanFactory.getBean(RebbitmqConstants.RABBITADMIN_NAME_PREFIX + instanceSignature, RabbitAdmin.class));
        //#resolveListenerQueues这个方法是声明队列、交互器和绑定的核心方法,因为涉及到多mq实例,为了避免bean冲突,使用各个mq实例的RabbitAdmin进行声明
        BindingParameterHolder holder = resolveListenerQueues(slimeRabbitListener, endpoint.getAdmin());
        endpoint.setQueueNames(holder.getQueueNames().toArray(new String[holder.getQueueNames().size()]));
        String containerFactoryName = resolveRabbitListenerContainerFactoryNameSuffix(
                RebbitmqConstants.RABBIT_MESSAGE_LISTENER_CONTAINER_FACTORY_NAME_PREFIX,
                instanceSignature,
                slimeRabbitListener.concurrentConsumers(), slimeRabbitListener.maxConcurrentConsumers());
        if (beanFactory.containsBean(containerFactoryName)) {
            containerFactory = beanFactory.getBean(containerFactoryName, SimpleRabbitListenerContainerFactory.class);
        } else {
            containerFactory = new SimpleRabbitListenerContainerFactory();
            CachingConnectionFactory connectionFactory = beanFactory.getBean(RebbitmqConstants.RABBIT_CONNECTION_FACTORY_NAME_PREFIX + instanceSignature,
                    CachingConnectionFactory.class);
            containerFactory.setConnectionFactory(connectionFactory);
            containerFactory.setAcknowledgeMode(AcknowledgeMode.AUTO);  //default auto ack
            containerFactory.setConcurrentConsumers(slimeRabbitListener.concurrentConsumers());
            containerFactory.setMaxConcurrentConsumers(slimeRabbitListener.maxConcurrentConsumers());
            containerFactory.setMessageConverter(contentTypeDelegatingMessageConverter());
            beanFactory.registerSingleton(containerFactoryName, containerFactory);
        }
        //这里有个比较蛋疼的问题,registrar里面有的containerFactoryName属性是通过beanFactory拿到RabbitListenerContainerFactory实例,
        //因此必须重复覆写containerFactoryName以确保在registrar拿到自定义注册的RabbitListenerContainerFactory,否则无法写入初始以及最大消费者数量属性
        //详细见RabbitListenerEndpointRegistrar#resolveContainerFactory
        this.registrar.setContainerFactoryBeanName(containerFactoryName);
        this.registrar.registerEndpoint(endpoint, containerFactory);
        //把注册的listener信息写入manager属性缓存
        RabbitmqRegistrarPropertiesManager.addConsumerBindingParameter(instanceSignature,
                resolveConsumerBindingParameter(slimeRabbitListener, bean.getClass().getCanonicalName(), holder));
    }

    private String resolveRabbitListenerContainerFactoryNameSuffix(String beanName, String instanceSignature, int initSize, int maxSize) {
        return beanName + instanceSignature + "[" + initSize + "," + maxSize + "]";
    }

    private String resolveEndpointId(SlimeRabbitListener slimeRabbitListener) {
        return "org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#"
				+ ArraysUtil.toString(slimeRabbitListener.queues()) + this.counter.getAndIncrement();
    }

    private BindingParameterHolder resolveListenerQueues(SlimeRabbitListener slimeRabbitListener, RabbitAdmin rabbitAdmin) {
        BindingParameterHolder holder = new BindingParameterHolder();
        String[] queues = slimeRabbitListener.queues();
        QueueBinding[] bindings = slimeRabbitListener.bindings();
        if (queues.length > 0 && bindings.length > 0) {
            throw new BeanInitializationException("@SlimeRabbitListener can have 'queues' or 'bindings' but not both");
        }
        if (queues.length == 0 && bindings.length == 0) {
            throw new BeanInitializationException("@SlimeRabbitListener must have 'queues' or 'bindings' but not both");
        }
        List<String> result = new ArrayList<>();
        if (queues.length > 0) {
            for (int i = 0; i < queues.length; i++) {
                Object resolvedValue = resolveExpression(queues[i]);
                resolveAsString(resolvedValue, result);
                holder.setQueueNames(result);
            }
        } else {
            registerBeansForDeclaration(slimeRabbitListener, rabbitAdmin, holder);
        }
        return holder;
    }

    private TypeMetadata buildListenersAnnotationMetadata(Class<?> targetClass) {
        Collection<SlimeRabbitListener> classLevelListeners = findListenerAnnotations(targetClass);
        final boolean hasClassLevelListeners = null != classLevelListeners && !classLevelListeners.isEmpty();
        final List<ListenerMethod> methods = new ArrayList<>();
        final List<Method> multiMethods = new ArrayList<>();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Collection<SlimeRabbitListener> listenerAnnotations = SlimeRabbitmqListenerAnnotationProcessor.this.findListenerAnnotations(method);
            if (null != listenerAnnotations && !listenerAnnotations.isEmpty()) {
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

    @SuppressWarnings("unchecked")
    private void resolveAsString(Object resolvedValue, List<String> result) {
        Object resolvedValueToUse = resolvedValue;
        if (resolvedValue instanceof String[]) {
            resolvedValueToUse = Arrays.asList((String[]) resolvedValue);
        }
        if (resolvedValueToUse instanceof org.springframework.amqp.core.Queue) {
            result.add(((org.springframework.amqp.core.Queue) resolvedValueToUse).getName());
        } else if (resolvedValueToUse instanceof String) {
            result.add((String) resolvedValueToUse);
        } else if (resolvedValueToUse instanceof Iterable) {
            for (Object object : (Iterable<Object>) resolvedValueToUse) {
                resolveAsString(object, result);
            }
        } else {
            throw new IllegalArgumentException(String.format(
                    "@RabbitListener can't resolve '%s' as either a String or a Queue",
                    resolvedValue));
        }
    }

    private void registerBeansForDeclaration(SlimeRabbitListener rabbitListener, RabbitAdmin rabbitAdmin,
                                             BindingParameterHolder holder) {
        if (this.beanFactory instanceof ConfigurableBeanFactory) {
            for (QueueBinding binding : rabbitListener.bindings()) {
                String queueName = declareQueue(binding, rabbitAdmin);
                List<String> queues = new ArrayList<>();
                queues.add(queueName);
                holder.setQueueNames(queues);
                declareExchangeAndBinding(binding, queueName, rabbitAdmin, holder);
            }
        }
    }

    private String declareQueue(QueueBinding binding, RabbitAdmin rabbitAdmin) {
        org.springframework.amqp.rabbit.annotation.Queue bindingQueue = binding.value();
        String queueName = (String) resolveExpression(bindingQueue.value());
        boolean exclusive = false;
        boolean autoDelete = false;
        if (!StringUtils.hasText(queueName)) {
            queueName = UUID.randomUUID().toString();
            // default exclusive/autodelete to true when anonymous
            if (!StringUtils.hasText(bindingQueue.exclusive())
                    || resolveExpressionAsBoolean(bindingQueue.exclusive())) {
                exclusive = true;
            }
            if (!StringUtils.hasText(bindingQueue.autoDelete())
                    || resolveExpressionAsBoolean(bindingQueue.autoDelete())) {
                autoDelete = true;
            }
        } else {
            exclusive = resolveExpressionAsBoolean(bindingQueue.exclusive());
            autoDelete = resolveExpressionAsBoolean(bindingQueue.autoDelete());
        }
        org.springframework.amqp.core.Queue queue = new org.springframework.amqp.core.Queue(queueName,
                resolveExpressionAsBoolean(bindingQueue.durable()),
                exclusive,
                autoDelete,
                resolveArguments(bindingQueue.arguments()));
        queue.setIgnoreDeclarationExceptions(resolveExpressionAsBoolean(bindingQueue.ignoreDeclarationExceptions()));
        rabbitAdmin.declareQueue(queue);
        return queueName;
    }

    private void declareExchangeAndBinding(QueueBinding binding, String queueName, RabbitAdmin rabbitAdmin,
                                           BindingParameterHolder holder) {
        org.springframework.amqp.rabbit.annotation.Exchange bindingExchange = binding.exchange();
        String exchangeName = resolveExpressionAsString(bindingExchange.value(), "@Exchange.exchange");
        String exchangeType = resolveExpressionAsString(bindingExchange.type(), "@Exchange.type");
        String routingKey = resolveExpressionAsString(binding.key(), "@QueueBinding.key");
        Exchange exchange;
        Binding actualBinding;
        if (exchangeType.equals(ExchangeTypes.DIRECT)) {
            exchange = directExchange(bindingExchange, exchangeName);
            actualBinding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey,
                    resolveArguments(binding.arguments()));
        } else if (exchangeType.equals(ExchangeTypes.FANOUT)) {
            exchange = fanoutExchange(bindingExchange, exchangeName);
            actualBinding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, "",
                    resolveArguments(binding.arguments()));
        } else if (exchangeType.equals(ExchangeTypes.TOPIC)) {
            exchange = topicExchange(bindingExchange, exchangeName);
            actualBinding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey,
                    resolveArguments(binding.arguments()));
        } else if (exchangeType.equals(ExchangeTypes.HEADERS)) {
            exchange = headersExchange(bindingExchange, exchangeName);
            actualBinding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey,
                    resolveArguments(binding.arguments()));
        } else {
            throw new BeanInitializationException("Unexpected exchange type: " + exchangeType);
        }
        AbstractExchange abstractExchange = (AbstractExchange) exchange;
        abstractExchange.setInternal(resolveExpressionAsBoolean(bindingExchange.internal()));
        abstractExchange.setDelayed(resolveExpressionAsBoolean(bindingExchange.delayed()));
        abstractExchange.setIgnoreDeclarationExceptions(resolveExpressionAsBoolean(bindingExchange.ignoreDeclarationExceptions()));
        actualBinding.setIgnoreDeclarationExceptions(resolveExpressionAsBoolean(binding.ignoreDeclarationExceptions()));
        rabbitAdmin.declareExchange(abstractExchange);
        rabbitAdmin.declareBinding(actualBinding);
        holder.setExchangeName(exchangeName);
        holder.setExchangeType(exchangeType);
        holder.setRoutingKey(routingKey);
    }

    private Exchange directExchange(org.springframework.amqp.rabbit.annotation.Exchange bindingExchange,
                                    String exchangeName) {
        return new DirectExchange(exchangeName,
                resolveExpressionAsBoolean(bindingExchange.durable()),
                resolveExpressionAsBoolean(bindingExchange.autoDelete()),
                resolveArguments(bindingExchange.arguments()));
    }

    private Exchange fanoutExchange(org.springframework.amqp.rabbit.annotation.Exchange bindingExchange,
                                    String exchangeName) {
        return new FanoutExchange(exchangeName,
                resolveExpressionAsBoolean(bindingExchange.durable()),
                resolveExpressionAsBoolean(bindingExchange.autoDelete()),
                resolveArguments(bindingExchange.arguments()));
    }

    private Exchange topicExchange(org.springframework.amqp.rabbit.annotation.Exchange bindingExchange,
                                   String exchangeName) {
        return new TopicExchange(exchangeName,
                resolveExpressionAsBoolean(bindingExchange.durable()),
                resolveExpressionAsBoolean(bindingExchange.autoDelete()),
                resolveArguments(bindingExchange.arguments()));
    }

    private Exchange headersExchange(org.springframework.amqp.rabbit.annotation.Exchange bindingExchange,
                                     String exchangeName) {
        return new HeadersExchange(exchangeName,
                resolveExpressionAsBoolean(bindingExchange.durable()),
                resolveExpressionAsBoolean(bindingExchange.autoDelete()),
                resolveArguments(bindingExchange.arguments()));
    }

    private Map<String, Object> resolveArguments(Argument[] arguments) {
        Map<String, Object> map = new HashMap<>();
        for (Argument arg : arguments) {
            String key = resolveExpressionAsString(arg.name(), "@Argument.name");
            if (StringUtils.hasText(key)) {
                Object value = resolveExpression(arg.value());
                Object type = resolveExpression(arg.type());
                Class<?> typeClass;
                String typeName;
                if (type instanceof Class) {
                    typeClass = (Class<?>) type;
                    typeName = typeClass.getName();
                } else {
                    Assert.isTrue(type instanceof String, "Type must resolve to a Class or String, but resolved to ["
                            + type.getClass().getName() + "]");
                    typeName = (String) type;
                    try {
                        typeClass = ClassUtils.forName(typeName, this.beanClassLoader);
                    } catch (Exception e) {
                        throw new IllegalStateException("Could not load class", e);
                    }
                }
                addToMap(map, key, value, typeClass, typeName);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("@Argument ignored because the name resolved to an empty String");
                }
            }
        }
        return map.size() < 1 ? null : map;
    }

    private void addToMap(Map<String, Object> map, String key, Object value, Class<?> typeClass, String typeName) {
        if (value.getClass().getName().equals(typeName)) {
            if (typeClass.equals(String.class) && !StringUtils.hasText((String) value)) {
                putEmpty(map, key);
            } else {
                map.put(key, value);
            }
        } else {
            if (value instanceof String && !StringUtils.hasText((String) value)) {
                putEmpty(map, key);
            } else {
                if (CONVERSION_SERVICE.canConvert(value.getClass(), typeClass)) {
                    map.put(key, CONVERSION_SERVICE.convert(value, typeClass));
                } else {
                    throw new IllegalStateException("Cannot convert from " + value.getClass().getName()
                            + " to " + typeName);
                }
            }
        }
    }

    private void putEmpty(Map<String, Object> map, String key) {
        if (this.emptyStringArguments.contains(key)) {
            map.put(key, "");
        } else {
            map.put(key, null);
        }
    }

    private boolean resolveExpressionAsBoolean(String value) {
        Object resolved = resolveExpression(value);
        if (resolved instanceof Boolean) {
            return (Boolean) resolved;
        } else if (resolved instanceof String) {
            return Boolean.valueOf((String) resolved);
        } else {
            return false;
        }
    }

    private String resolveExpressionAsString(String value, String attribute) {
        Object resolved = resolveExpression(value);
        if (resolved instanceof String) {
            return (String) resolved;
        } else {
            throw new IllegalStateException("The [" + attribute + "] must resolve to a String. "
                    + "Resolved to [" + resolved.getClass() + "] for [" + value + "]");
        }
    }

    private Object resolveExpression(String value) {
        String resolvedValue = resolve(value);

        if (!(resolvedValue.startsWith("#{") && value.endsWith("}"))) {
            return resolvedValue;
        }

        return this.resolver.evaluate(resolvedValue, this.expressionContext);
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

    private ConsumerBindingParameter resolveConsumerBindingParameter(SlimeRabbitListener slimeRabbitListener, String className, BindingParameterHolder holder) {
        ConsumerBindingParameter consumerBindingParameter = new ConsumerBindingParameter();
        int nameSize = holder.getQueueNames().size();
        if (1 == nameSize) {
            consumerBindingParameter.setQueueName(holder.getQueueNames().get(0));
        } else {
            consumerBindingParameter.setQueueName(holder.getQueueNames().toString());
        }
        consumerBindingParameter.setAcknowledgeMode("AUTO");
        consumerBindingParameter.setConcurrentConsumers(slimeRabbitListener.concurrentConsumers());
        consumerBindingParameter.setMaxConcurrentConsumers(slimeRabbitListener.maxConcurrentConsumers());
        consumerBindingParameter.setListenerClassName(className);
        consumerBindingParameter.setExchangeName(holder.getExchangeName());
        consumerBindingParameter.setExchangeType(holder.getExchangeType());
        consumerBindingParameter.setRoutingKey(holder.getRoutingKey());
        return consumerBindingParameter;
    }

    private static class BindingParameterHolder {

        private List<String> queueNames;
        private String exchangeName;
        private String exchangeType;
        private String routingKey;

        public BindingParameterHolder() {
        }

        public List<String> getQueueNames() {
            return queueNames;
        }

        public void setQueueNames(List<String> queueNames) {
            this.queueNames = queueNames;
        }

        public String getExchangeName() {
            return exchangeName;
        }

        public void setExchangeName(String exchangeName) {
            this.exchangeName = exchangeName;
        }

        public String getExchangeType() {
            return exchangeType;
        }

        public void setExchangeType(String exchangeType) {
            this.exchangeType = exchangeType;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }
    }

    private ContentTypeDelegatingMessageConverter contentTypeDelegatingMessageConverter() {
        ContentTypeDelegatingMessageConverter converter = new ContentTypeDelegatingMessageConverter();
        converter.addDelegate(MediaType.APPLICATION_JSON_VALUE, new Jackson2JsonMessageConverter());
        converter.addDelegate(MediaType.TEXT_PLAIN_VALUE, new Jackson2JsonMessageConverter());
        return converter;
    }
}
