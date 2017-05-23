package org.throwable.utils;

import jodd.util.StringUtil;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.env.Environment;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/22 17:03
 */
public final class EnvironmentUtils {

	private static Map<Class<?>, Class<?>> COLLECTION_INTERFACES_MAPPINGS = new HashMap<>();

	static {
		COLLECTION_INTERFACES_MAPPINGS.put(List.class, ArrayList.class);
		COLLECTION_INTERFACES_MAPPINGS.put(Map.class, HashMap.class);
		COLLECTION_INTERFACES_MAPPINGS.put(Set.class, HashSet.class);
	}

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
					Class<?> targetType = descriptor.getPropertyType();
					if (ClazzUtils.isPrimitive(targetType)) {
						Object value = resolver.getProperty(descriptor.getName(), targetType);
						if (null == value) {  //环境变量拿到为空,尝试从实体Field获取
							Field targetField = targetObject.getClass().getDeclaredField(descriptor.getName());
							if (null != targetField) {
								targetField.setAccessible(true);
								value = targetField.get(targetObject);
								targetField.setAccessible(false);
							}
						}
						descriptor.getWriteMethod().invoke(targetObject, value);
					} else if (Collection.class.isAssignableFrom(targetType)) { //集合类型,遍历解析
						String subfix = prefix + descriptor.getName();
						Object collection = parseCollectionTypeEnvProperties(subfix, env, targetType);
						descriptor.getWriteMethod().invoke(targetObject, collection);
					} else {  //非集合非基础类型,迭代解析
						String suffix = prefix + descriptor.getName() + ".";
						Object subObject = parseEnvironmentPropertiesToBean(env, targetType, suffix);
						descriptor.getWriteMethod().invoke(targetObject, subObject);
					}
				}
			}
			return (T) targetObject;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}


	@SuppressWarnings("unchecked")
	private static Object parseCollectionTypeEnvProperties(String prefix, Environment env, Class<?> collectionType) throws Exception {
		collectionType = COLLECTION_INTERFACES_MAPPINGS.getOrDefault(collectionType, ArrayList.class);
		Collection collection = (Collection) collectionType.newInstance();
		int index = 0;
		while (null != env.getProperty(prefix + "[" + index + "]")) {
			collection.add(env.getProperty(prefix + "[" + index + "]"));
			index++;
		}
		return collection;
	}

}
