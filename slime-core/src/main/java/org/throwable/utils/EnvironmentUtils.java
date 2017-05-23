package org.throwable.utils;

import jodd.util.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.Environment;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/22 17:03
 */
public final class EnvironmentUtils {

    @SuppressWarnings("unchecked")
    public static <T> T parseEnvironmentPropertiesToBean(Environment env, Class<?> target, String prefix) {
        if (StringUtil.isBlank(prefix)) {
            return null;
        }
        if (!prefix.endsWith(".")) {
            prefix = prefix + ".";
        }
        try {
            RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(env, prefix);
            Object targetObject = target.newInstance();
            PropertyDescriptor[] descriptors = BeanInfoUtils.findAllDescriptorsByType(target);
            for (PropertyDescriptor descriptor : descriptors) {
                if (!descriptor.getName().equals("class")) {
                    Object value = resolver.getProperty(descriptor.getName(), descriptor.getPropertyType());
                    if (ClazzUtils.isPrimitive(descriptor.getPropertyType())) {
                        descriptor.getWriteMethod().invoke(targetObject, value);
                    } else if (Collection.class.isAssignableFrom(descriptor.getPropertyType())) { //集合类型,再遍历解析
                        String subfix = prefix + descriptor.getName() + ".";
                        Collection collection = env.getProperty(subfix, Collection.class);
                        descriptor.getWriteMethod().invoke(targetObject, collection);
                    } else {  //非集合非基础类型
                        String subfix = prefix + descriptor.getName() + ".";
                        Object subObject = parseEnvironmentPropertiesToBean(env, descriptor.getPropertyType(), subfix);
                        descriptor.getWriteMethod().invoke(targetObject, subObject);
                    }
                }
            }
            return (T) targetObject;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
