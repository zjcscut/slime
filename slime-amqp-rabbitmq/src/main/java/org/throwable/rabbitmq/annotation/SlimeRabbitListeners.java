package org.throwable.rabbitmq.annotation;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/26 9:40
 */
@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface SlimeRabbitListeners {

    SlimeRabbitListener[] value();
}
