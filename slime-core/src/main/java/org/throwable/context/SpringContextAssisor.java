package org.throwable.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/16 5:17
 */
public final class SpringContextAssisor implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		SpringContextAssisor.applicationContext = applicationContext;
	}

	public static Object getBeanDefinition(String name) {
		return applicationContext.getBean(name);
	}

	public static <T> T getBeanDefinition(String name, Class<T> clazz) {
		return applicationContext.getBean(name, clazz);
	}

}
