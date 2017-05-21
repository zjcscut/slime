package org.throwable.lock.annotation;

import org.throwable.lock.common.LockPolicyEnum;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/5/17 11:59
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    LockPolicyEnum policy();

    Class<?> target() default String.class;

    String keyName();

    long waitSeconds() default -1;

}
