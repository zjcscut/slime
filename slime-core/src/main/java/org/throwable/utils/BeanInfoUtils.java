package org.throwable.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/20 13:53
 */
public final class BeanInfoUtils {

	public static PropertyDescriptor[] findAllDescriptorsByType(Class<?> type){
		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(type);
		} catch (IntrospectionException e) {
			throw new IllegalStateException(e);
		}
		return beanInfo.getPropertyDescriptors();
	}
}
