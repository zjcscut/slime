package org.throwable.context;

import jodd.util.StringUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.Assert;
import org.throwable.exception.BeanRegisterHandleException;
import org.throwable.utils.Strings;

import java.util.Map;

/**
 * @author throwable
 * @version 2017/1/14 17:26
 * @function BeanDefinition构件处理工厂
 */
public final class BeanRegisterComponentFactory {


	/**
	 * @param component bean定义构件
	 * @return BeanDefinition
	 */
	public static BeanDefinition processBeanDefinitionComponent(BeanDefinitionComponent component) {
		Assert.notNull(component, "BeanDefinitionComponent must not be null");
		BeanDefinitionBuilder builder;
		if (StringUtil.isNotBlank(component.getClassName())) {
			builder = BeanDefinitionBuilder.genericBeanDefinition(component.getClassName());
		} else if (null != component.getBeanClass()) {
			builder = BeanDefinitionBuilder.genericBeanDefinition(component.getBeanClass());
		} else {
			throw new BeanRegisterHandleException("create BeanDefinition failed,className or beanClass must be defined");
		}

		if (null != component.getBeanPropertyValues() && !component.getBeanPropertyValues().isEmpty()) {
			for (Map.Entry<String, Object> entry : component.getBeanPropertyValues().entrySet()) {
				builder.addPropertyValue(entry.getKey(), entry.getValue());
			}
		}
		if (null != component.getBeanPropertyReferences() && !component.getBeanPropertyReferences().isEmpty()) {
			for (Map.Entry<String, String> entry : component.getBeanPropertyReferences().entrySet()) {
				builder.addPropertyReference(entry.getKey(), entry.getValue());
			}
		}

		if (null != component.getDependsOn() && !component.getDependsOn().isEmpty()) {
			for (String o : component.getDependsOn()) {
				builder.addDependsOn(o);
			}
		}

		if (null != component.getConstructorArgValues() && !component.getConstructorArgValues().isEmpty()) {
			for (Object argValue : component.getConstructorArgValues()) {
				builder.addConstructorArgValue(argValue);
			}
		}

		if (null != component.getConstructorArgReferences() && !component.getConstructorArgReferences().isEmpty()) {
			for (String argValueRef : component.getConstructorArgReferences()) {
				builder.addConstructorArgReference(argValueRef);
			}
		}

		if (StringUtil.isBlank(component.getBeanName())) {
			if (StringUtil.isNotBlank(component.getClassName())) {
				component.setBeanName(Strings.firstCharToLowerCase(component.getClassName()));
			} else {
				component.setBeanName(Strings.firstCharToLowerCase(component.getBeanClass().getSimpleName()));
			}
		}

		if (StringUtil.isNotBlank(component.getInitMethod())) {
			builder.setInitMethodName(component.getInitMethod());
		}

		if (StringUtil.isNotBlank(component.getDestoryMethod())) {
			builder.setDestroyMethodName(component.getDestoryMethod());
		}
		return builder.getRawBeanDefinition();
	}

}
