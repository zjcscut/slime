package org.throwable.rabbitmq.annotation;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/28 12:06
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
@Documented
public @interface SlimeRabbitListeners {

	SlimeRabbitListener[] value();
}
