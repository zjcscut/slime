package org.throwable.distributed.support;

import lombok.extern.slf4j.Slf4j;
import org.throwable.distributed.exception.UnMatchedLockKeyException;
import org.throwable.utils.BeanInfoUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/18 1:42
 */
@Slf4j
public class DistributedLockTargetKeyMatcher {

	private final Map<Class<?>, PropertyDescriptor[]> descriptors = new ConcurrentHashMap<>();

	private PropertyDescriptor[] initDescriptorByType(Class<?> clazz) {
		PropertyDescriptor[] propertyDescriptors = descriptors.get(clazz);
		if (null != propertyDescriptors) {
			return propertyDescriptors;
		} else {
			propertyDescriptors = BeanInfoUtils.findAllDescriptorsByType(clazz);
			descriptors.put(clazz, propertyDescriptors);
			return propertyDescriptors;
		}
	}

	private PropertyDescriptor fetchDescriptorByTypeAndKey(Class<?> clazz, String key) {
		PropertyDescriptor[] propertyDescriptors = initDescriptorByType(clazz);
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			if (propertyDescriptor.getName().equals(key)) {
				return propertyDescriptor;
			}
		}
		throw new UnMatchedLockKeyException(String.format("PropertyDescriptor of keyName:[%s] could not be found!!!!", key));
	}

	public String matchAndReturnLockKeyByTargetObjectAndKeyName(Object target, String key) {
		PropertyDescriptor propertyDescriptor = fetchDescriptorByTypeAndKey(target.getClass(), key);
		Method readMethod = propertyDescriptor.getReadMethod();
		try {
			Object targetValue = readMethod.invoke(target, (Object[]) null);
			if (null == targetValue) {
				throw new UnMatchedLockKeyException(String.format("TargetValue of keyName:[%s] must not null!!!!", key));
			} else if (targetValue.getClass().isAssignableFrom(String.class)) {
				return (String) targetValue;
			} else {
				return targetValue.toString();
			}
		} catch (Exception e) {
			log.error(String.format("Read targetValue of keyName:[%s] failed!!!!Target type:<%s>"
					, key, target.getClass().getCanonicalName()), e);
			throw new UnMatchedLockKeyException(String.format("read targetValue of keyName:[%s] failed!!!!Target type:<%s>"
					, key, target.getClass().getCanonicalName()));
		}
	}

	public String matchAndReturnLockKeyByTargetObject(Object target, String keySeparator, String[] keyNameArray) {
		StringBuilder result = new StringBuilder();
		for (String keyName : keyNameArray) {
			result.append(matchAndReturnLockKeyByTargetObjectAndKeyName(target, keyName)).append(keySeparator);
		}
		return result.substring(0, result.lastIndexOf(keySeparator));
	}

}
