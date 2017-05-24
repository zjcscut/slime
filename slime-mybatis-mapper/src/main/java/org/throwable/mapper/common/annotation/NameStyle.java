package org.throwable.mapper.common.annotation;

import org.throwable.mapper.common.constant.NameStyleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author throwable
 * @version v1.0
 * @description 命名转换规范
 * @since 2017/3/30 12:56
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NameStyle {

	NameStyleEnum value() default NameStyleEnum.NORMAL;
}
