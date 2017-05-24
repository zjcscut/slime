package org.throwable.mapper.utils;

import com.google.common.collect.Lists;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/2 12:55
 */
public final class ReflectionUtils {


	/**
	 * 循环向上获取整个类继承体系中的全部属性,把父类属性排列在前
	 *
	 * @param clazz clazz
	 * @return list
	 */
	public static List<Field> getDeclaredFields(Class<?> clazz) {
		Assert.notNull(clazz, "Class<?> object must not be null");
		LinkedList<Field> fields = Lists.newLinkedList();
		for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
			try {
				if (!Object.class.equals(clazz)
						&& (clazz.isAnnotationPresent(Entity.class))
						|| (!Map.class.isAssignableFrom(clazz)
						&& !Collection.class.isAssignableFrom(clazz))) {
					Field[] fieldArrays = clazz.getDeclaredFields();
					for (Field field : fieldArrays) {
						fields.addFirst(field);
					}
				}
			} catch (Exception e) {
				//ignore
			}
		}
		return fields;
	}
}
