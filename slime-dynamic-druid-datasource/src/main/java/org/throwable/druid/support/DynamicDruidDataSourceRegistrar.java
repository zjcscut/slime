package org.throwable.druid.support;

import com.alibaba.druid.pool.DruidDataSource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.throwable.druid.annotation.AutoDynamicDataSource;
import org.throwable.druid.configuration.DruidInstance;
import org.throwable.druid.configuration.DruidInstanceProperties;
import org.throwable.druid.configuration.SlimeDruidProperties;
import org.throwable.utils.BeanUtils;
import org.throwable.utils.JacksonUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/1 16:35
 */
@Slf4j
public class DynamicDruidDataSourceRegistrar implements EnvironmentAware, ImportBeanDefinitionRegistrar, SmartInitializingSingleton {

	private final Set<String> druidSignaturesCache = new HashSet<>();
	private DruidInstanceProperties druids;
	private SimpleRoutingDataSource simpleRoutingDataSource;
	private DefaultListableBeanFactory beanFactory;

	@Override
	public void setEnvironment(Environment environment) {
		String location = environment.getProperty(SlimeDruidProperties.PREFIX + ".location");
		Assert.hasText(location, "Dynamic datasource properties location must not be empty!");
		druids = JacksonUtils.parseFromJsonFile(location, DruidInstanceProperties.class);
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
										BeanDefinitionRegistry registry) {
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
		this.beanFactory = beanFactory;
		processRegisterDynamicDruidDataSources(beanFactory);
		registerAutoDynamicDruidDataSourceAspect(beanFactory);
		registerDynamicDruidTemplate(beanFactory);
	}

	@Override
	public void afterSingletonsInstantiated() {
		if (null == this.simpleRoutingDataSource) {
			this.simpleRoutingDataSource = beanFactory.getBean(SimpleRoutingDataSource.class);
		}
		simpleRoutingDataSource.afterPropertiesSet();
	}

	private void processRegisterDynamicDruidDataSources(DefaultListableBeanFactory beanFactory) {
		Assert.isTrue(null != druids && null != druids.getDruids() && !druids.getDruids().isEmpty(),
				"Parse properties from json file failed!");
		List<DruidInstance> druidInstances = druids.getDruids();
		List<Boolean> primaryList = druidInstances.stream().map(DruidInstance::getPrimary).collect(Collectors.toList());
		checkPrimaryList(primaryList);
		List<String> signatureList = druidInstances.stream().map(DruidInstance::getSignature).collect(Collectors.toList());
		checkSignatureList(signatureList);
		DruidInstance primaryInstance = druidInstances.stream().filter(DruidInstance::getPrimary).collect(Collectors.toList()).get(0);
		Map<Object, Object> druidDataSources = new HashMap<>();
		for (DruidInstance instance : druidInstances) {
			druidDataSources.put(instance.getSignature(), processResolvingDruidPropertiesAndCreation(instance));
			druidSignaturesCache.add(instance.getSignature());
		}
		DruidDataSource defaultDataSource = processResolvingDruidPropertiesAndCreation(primaryInstance);
		processRegisterRoutingDruidDataSource(beanFactory, druidDataSources, defaultDataSource);
		processRegisterAllDruidDataSource(beanFactory, druidDataSources);
	}

	private void processRegisterRoutingDruidDataSource(DefaultListableBeanFactory beanFactory,
													   Map<Object, Object> druidDataSources,
													   DruidDataSource defaultDataSource) {
		String beanName = "dataSource";
		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(SimpleRoutingDataSource.class);
		abd.setScope(BeanDefinition.SCOPE_SINGLETON);  //单例
		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd); //扫描类上注解进行配置
		abd.addQualifier(new AutowireCandidateQualifier(beanName));  //默认使用"dataSource"进行自动byName注入
		MutablePropertyValues mutablePropertyValues = new MutablePropertyValues();
		mutablePropertyValues.addPropertyValue("defaultTargetDataSource", defaultDataSource);
		mutablePropertyValues.addPropertyValue("targetDataSources", druidDataSources);
		abd.setPropertyValues(mutablePropertyValues);
		//其实这里可以用GenericBeanDefinition进行bean注册,效果差不多
		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, beanFactory);
	}

	private void processRegisterAllDruidDataSource(DefaultListableBeanFactory beanFactory,
												   Map<Object, Object> druidDataSources) {
		for (Map.Entry<Object, Object> entry : druidDataSources.entrySet()) {
			//这里要注意一点,因为DruidDataSource实现了java.sql.DataSource,涉及到Spring容器中一个接口的多个实例,如果直接注册为单例会抛出异常
			//因为容器在获取java.sql.DataSource接口类型的所有bean的时候,获取到的不是唯一实现,这个时候可以参考@Primary以及@Qualifier的实现逻辑
			String beanName = "druidDataSource-" + entry.getKey().toString();
			beanFactory.registerSingleton(beanName, entry.getValue());
		}
	}

	private void checkPrimaryList(List<Boolean> primaryList) {
		Assert.isTrue(primaryList.stream().anyMatch(one -> null != one && one.equals(Boolean.TRUE)),
				"Primary druid datasource must be defined!Please check the configuration field primary!");
		Set<Boolean> filter = new HashSet<>();
		for (Boolean one : primaryList) {
			if (filter.contains(one)) {
				throw new IllegalArgumentException("Only one primary druid datasource can be defined!Please check the configuration field primary!");
			} else {
				filter.add(one);
			}
		}
	}

	private void checkSignatureList(List<String> signatureList) {
		Assert.isTrue(signatureList.stream().allMatch(StringUtil::isNotBlank), "Signature of druid datasource must be defined!");
		Set<String> filter = new HashSet<>();
		for (String one : signatureList) {
			if (filter.contains(one)) {
				throw new IllegalArgumentException("The Signature of druid datasource configuration must be exclusive globally!");
			} else {
				filter.add(one);
			}
		}
	}

	private DruidDataSource processResolvingDruidPropertiesAndCreation(DruidInstance instance) {
		DruidDataSource druidDataSource = new DruidDataSource();
		Assert.hasText(instance.getUrl(), "DruidDataSource#url must not be empty!");
		Assert.hasText(instance.getUsername(), "DruidDataSource#username must not be empty!");
		Assert.hasText(instance.getDriverClassName(), "DruidDataSource#driverClassName must not be empty!");
		Assert.hasText(instance.getPassword(), "DruidDataSource#password must not be empty!");
		BeanUtils.copyPropertiesIgnoreSourceNullProperties(instance, druidDataSource);
		druidDataSource.setName(instance.getSignature());
		return druidDataSource;
	}

	private void registerAutoDynamicDruidDataSourceAspect(DefaultListableBeanFactory beanFactory) {
		Advice advice = (MethodInterceptor) invocation -> {
			ProxyMethodInvocation pmi = (ProxyMethodInvocation) invocation;
			ProceedingJoinPoint pjp = new MethodInvocationProceedingJoinPoint(pmi);
			return processAutoDynamicDruidDataSourceAspect(invocation, pjp);
		};
		AspectJExpressionPointcutAdvisor autoDynamicDruidDataSourceAspect = new AspectJExpressionPointcutAdvisor();
		autoDynamicDruidDataSourceAspect.setLocation("$$dynamicDruidDataSourceAspect##");
		autoDynamicDruidDataSourceAspect.setExpression("@annotation(org.throwable.druid.annotation.AutoDynamicDataSource)");
		autoDynamicDruidDataSourceAspect.setAdvice(advice);
		autoDynamicDruidDataSourceAspect.setBeanFactory(beanFactory);
		beanFactory.registerSingleton("autoDynamicDruidDataSourceAspect", autoDynamicDruidDataSourceAspect);
	}

	private Object processAutoDynamicDruidDataSourceAspect(MethodInvocation methodInvocation, ProceedingJoinPoint joinPoint) {
		try {
			//获取目标方法,有可能是接口方法
			Method targetMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
			//注解必须放在实现方法
			Method currentMethod = joinPoint.getTarget().getClass().getDeclaredMethod(targetMethod.getName(), targetMethod.getParameterTypes());
			AutoDynamicDataSource annotation = AnnotationUtils.findAnnotation(currentMethod, AutoDynamicDataSource.class);
			String value = annotation.value();
			Assert.isTrue(druidSignaturesCache.contains(value), String.format("Signature of [%s] has not be found in dataSources have be registered!", value));
			DataSourceHolder.bindResource(value);
			return methodInvocation.proceed();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			DataSourceHolder.removeResource();
		}
	}

	private void registerDynamicDruidTemplate(DefaultListableBeanFactory beanFactory) {
		DynamicDruidTemplate dynamicDruidTemplate = new DynamicDruidTemplate();
		beanFactory.registerSingleton("dynamicDruidTemplate", dynamicDruidTemplate);
	}
}
