package org.throwable.mapper.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author throwable
 * @version v1.0
 * @description 实体域
 * @since 2017/3/30 12:48
 */

@Setter
@Getter
public class EntityField {

	private String name;
	private Field field;
	private Class<?> javaType;
	private Method setter;
	private Method getter;

	/**
	 * 解析反射获取的域属性
	 */
	public EntityField(Field field) {
		if (null != field) {
			this.field = field;
			this.name = field.getName();
			this.javaType = field.getType();
		}
	}

	/**
	 * 解析内省获取的属性
	 */
	public EntityField(PropertyDescriptor propertyDescriptor) {
		if (null != propertyDescriptor) {
			this.name = propertyDescriptor.getName();
			this.setter = propertyDescriptor.getWriteMethod();
			this.getter = propertyDescriptor.getReadMethod();
			this.javaType = propertyDescriptor.getPropertyType();
		}
	}


	public void copy(EntityField other) {
		this.setter = other.setter;
		this.getter = other.getter;
		this.javaType = other.javaType;
		this.name = other.name;
	}


	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		boolean result = false;
		if (field != null) {
			result = field.isAnnotationPresent(annotationClass);
		}
		if (!result && setter != null) {
			result = setter.isAnnotationPresent(annotationClass);
		}
		if (!result && getter != null) {
			result = getter.isAnnotationPresent(annotationClass);
		}
		return result;
	}


	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		T result = null;
		if (field != null) {
			result = field.getAnnotation(annotationClass);
		}
		if (result == null && setter != null) {
			result = setter.getAnnotation(annotationClass);
		}
		if (result == null && getter != null) {
			result = getter.getAnnotation(annotationClass);
		}
		return result;
	}



	public void setJavaType(Class<?> javaType) {
		this.javaType = javaType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EntityField that = (EntityField) o;
		return !(name != null ? !name.equals(that.name) : that.name != null);
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}


}
