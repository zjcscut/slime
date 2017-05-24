package org.throwable.mapper.common.annotation;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author throwable
 * @version v1.0
 * @description 实体行扩展注解
 * @since 2017/3/30 12:52
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ColumnExtend {

	String column() default "";

	JdbcType jdbcType() default JdbcType.UNDEFINED;

	Class<? extends TypeHandler<?>> typeHandler() default UnknownTypeHandler.class;
}
