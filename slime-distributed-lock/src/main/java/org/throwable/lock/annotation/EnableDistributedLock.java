package org.throwable.lock.annotation;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.throwable.lock.configuration.DistributedLockProperties;
import org.throwable.lock.support.DistributedLockAspectRegistrar;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/5/17 11:58
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableConfigurationProperties(value = DistributedLockProperties.class)
@Import(DistributedLockAspectRegistrar.class)
public @interface EnableDistributedLock {
}
