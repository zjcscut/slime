package org.throwable.context;


/**
 * @author throwable
 * @version 2017/1/14 16:25
 * @function bean注册处理器接口
 */
public interface BeanRegisterHandler {

    void registerBeanDefinition(BeanDefinitionComponent component);

    Class<?> loadContextClass(String className);

    Object loadBeanFromContext(String beanName);

    <T> T loadBeanFromContext(String beanName, Class<T> clazz);

    <T> T loadBeanFromContext(Class<T> clazz);

    void removeBeanFromContext(String beanName);
}
