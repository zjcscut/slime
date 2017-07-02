package org.throwable.druid.annotation;

import org.springframework.context.annotation.Import;
import org.throwable.druid.support.DynamicDruidDataSourceRegistrar;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/1 16:34
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(value = {DynamicDruidDataSourceRegistrar.class})
public @interface EnableDynamicDruidDataSource {

}
