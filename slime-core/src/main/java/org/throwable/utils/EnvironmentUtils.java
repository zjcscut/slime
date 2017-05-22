package org.throwable.utils;

import jodd.util.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.Environment;

import java.beans.PropertyDescriptor;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/22 17:03
 */
public final class EnvironmentUtils {

    public static <T> T parseEnvironmentPropertiesToBean(Environment env, Class<T> target, String prefix) {
        if (StringUtil.isBlank(prefix)) {
            return null;
        }
        if (!prefix.endsWith(".")) {
            prefix = prefix + ".";
        }
        RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(env, prefix);
        T t = BeanUtils.instantiateClass(target);
        PropertyDescriptor[] descriptors = BeanInfoUtils.findAllDescriptorsByType(target);
        try {
            for (PropertyDescriptor descriptor : descriptors) {
                if (!descriptor.getName().equals("class")) {
                    descriptor.getWriteMethod().invoke(t, resolver.getProperty(descriptor.getName(), descriptor.getPropertyType()));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return t;
    }
}
