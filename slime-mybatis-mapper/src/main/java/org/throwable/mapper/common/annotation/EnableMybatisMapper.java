package org.throwable.mapper.common.annotation;

import org.springframework.context.annotation.Import;
import org.throwable.mapper.configuration.AutoConfiguredMapperScannerRegistrar;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/24 20:18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AutoConfiguredMapperScannerRegistrar.class)
public @interface EnableMybatisMapper {

}
