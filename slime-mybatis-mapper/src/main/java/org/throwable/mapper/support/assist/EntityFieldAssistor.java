package org.throwable.mapper.support.assist;


import com.google.common.collect.Lists;
import org.throwable.exception.BeanReflectionException;
import org.throwable.mapper.common.entity.EntityField;
import org.throwable.mapper.utils.ReflectionUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description 实体域
 * @since 2017/3/30 12:48
 */
public class EntityFieldAssistor {

	/**
	 * 合并反射和方法属性
	 *
	 * @param entityClass
	 * @return
	 */
	public static List<EntityField> getEntityFieldsProperties(Class<?> entityClass) {
		List<EntityField> fields = getEntityFields(entityClass);
		List<EntityField> properties = getEntityProperties(entityClass);
		//#拷贝set、get方法到反射属性中
		List<EntityField> all = Lists.newArrayList();
		for (EntityField field : fields) {
			for (EntityField property : properties) {
				if (field.getName().equals(property.getName())) {
					field.copy(property);
				}
			}
			all.add(field);
		}
		return all;
	}

	/**
	 * 反射获取字段属性
	 *
	 * @param entityClass
	 * @return
	 */
	public static List<EntityField> getEntityFields(Class<?> entityClass) {
		List<EntityField> entityFields = Lists.newArrayList();
		List<Field> fields = ReflectionUtils.getDeclaredFields(entityClass);
		for (Field field : fields) {
			//#排除静态字段和非持久化字段
			if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
				entityFields.add(new EntityField(field));
			}
		}
		return entityFields;
	}

	/**
	 * 通过get、set方法获取属性
	 *
	 * @param entityClass
	 * @return
	 */
	public static List<EntityField> getEntityProperties(Class<?> entityClass) {
		List<EntityField> entityFields = Lists.newArrayList();
		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(entityClass);
		} catch (IntrospectionException e) {
			throw new BeanReflectionException(e);
		}
		PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor descriptor : descriptors) {
			if ("class".equals(descriptor.getName())) {  //#排除解析得到的PropertyDescriptor数组中会包含一个class的属性
				continue;
			}
			entityFields.add(new EntityField(descriptor));
		}
		return entityFields;
	}

}
