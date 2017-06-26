package org.throwable.redisson.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/25 1:27
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonDistributedLock {

	String lockPathPrefix() default "REDISSON_LOCK_KEY_";

	String keySeparator() default "_";

	String[] keyNames();

	long waitTime() default 5000;

	long leaseTime() default 15000;

	TimeUnit unit() default TimeUnit.MILLISECONDS;

	int order() default 1;

	boolean isFair() default false;
}
