package org.throwable.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.throwable.exception.BeanRegisterHandleException;

/**
 * @author throwable
 * @version 2017/1/14 16:28
 * @function 默认Bean注册处理器
 */
@Service
public class DefaultBeanRegisterHandler implements BeanRegisterHandler {

	@Autowired
	private DefaultListableBeanFactory defaultListableBeanFactory;

	@Override
	public void registerBeanDefinition(BeanDefinitionComponent component) {
		BeanDefinition beanDefinition = BeanRegisterComponentFactory.processBeanDefinitionComponent(component);
		defaultListableBeanFactory.registerBeanDefinition(component.getBeanName(), beanDefinition);
	}

	@Override
	public Class<?> loadContextClass(String className) {
		try {
			return ClassUtils.getDefaultClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new BeanRegisterHandleException(e);
		}
	}

	@Override
	public Object loadBeanFromContext(String beanName) {
		return defaultListableBeanFactory.getBean(beanName);
	}

	@Override
	public <T> T loadBeanFromContext(String beanName, Class<T> clazz) {
		return defaultListableBeanFactory.getBean(beanName, clazz);
	}

	@Override
	public <T> T loadBeanFromContext(Class<T> clazz) {
		return defaultListableBeanFactory.getBean(clazz);
	}

	@Override
	public void removeBeanFromContext(String beanName) {
		if (defaultListableBeanFactory.containsBeanDefinition(beanName)) {
			defaultListableBeanFactory.removeBeanDefinition(beanName);
		}
	}

}
