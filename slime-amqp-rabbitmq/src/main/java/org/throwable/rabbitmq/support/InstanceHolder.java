package org.throwable.rabbitmq.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.throwable.rabbitmq.configuration.RabbitmqInstanceProperties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 15:11
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InstanceHolder<T extends RabbitmqInstanceProperties> {

	private String instanceSign;
	private T instance;

}
