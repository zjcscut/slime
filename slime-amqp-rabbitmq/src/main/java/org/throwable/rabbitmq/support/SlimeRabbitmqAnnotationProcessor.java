package org.throwable.rabbitmq.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.*;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ReflectionUtils;
import org.throwable.rabbitmq.annotation.SlimeRabbitHandler;
import org.throwable.rabbitmq.annotation.SlimeRabbitListener;
import org.throwable.rabbitmq.annotation.SlimeRabbitListeners;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author throwable
 * @version v1.0
 * @description refer to {@link org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor}
 * @since 2017/5/30 3:33
 */
@Slf4j
public class SlimeRabbitmqAnnotationProcessor
		implements BeanFactoryAware, BeanPostProcessor, Ordered, SmartInitializingSingleton {

	private static final ConversionService CONVERSION_SERVICE = new DefaultConversionService();

	private final ConcurrentMap<Class<?>, TypeMetadata> typeCache = new ConcurrentHashMap<>();

	private BeanExpressionResolver resolver = new StandardBeanExpressionResolver();

	private BeanExpressionContext expressionContext;

	private ConfigurableBeanFactory beanFactory;

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
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
				processAmqpListener(slimeRabbitListener, lm.method, bean, beanName);
			}
		}
		if (typeMetadata.handlerMethods.length > 0) { //use class level @SlimeRabbitListener and @SlimeRabbitHandler

		}
		return bean;
	}

	@Override
	public void afterSingletonsInstantiated() {

	}

	protected void processAmqpListener(SlimeRabbitListener slimeRabbitListener, Method method, Object bean, String beanName) {

	}

	private TypeMetadata buildListenersAnnotationMetadata(Class<?> targetClass) {
		Collection<SlimeRabbitListener> classLevelListeners = findListenerAnnotations(targetClass);
		final boolean hasClassLevelListeners = classLevelListeners.size() > 0;
		final List<ListenerMethod> methods = new ArrayList<>();
		final List<Method> multiMethods = new ArrayList<>();
		ReflectionUtils.doWithMethods(targetClass, method -> {
			Collection<SlimeRabbitListener> listenerAnnotations = SlimeRabbitmqAnnotationProcessor.this.findListenerAnnotations(method);
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
}
