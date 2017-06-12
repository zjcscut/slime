package org.throwable.rabbitmq.support;

import org.throwable.rabbitmq.configuration.RabbitmqInstanceProperties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 15:11
 */
public class InstanceHolder<T extends RabbitmqInstanceProperties> {

	private T instance;

	protected String instanceSignature;

	public T getInstance() {
		return instance;
	}

	public void setInstance(T instance) {
		this.instance = instance;
	}

	public String getinstanceSignature() {
		return instanceSignature;
	}

	public void setinstanceSignature(String instanceSignature) {
		this.instanceSignature = instanceSignature;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		InstanceHolder<?> that = (InstanceHolder<?>) o;

		return instance != null ? instance.equals(that.instance) : that.instance == null;
	}

	@Override
	public int hashCode() {
		return instance != null ? instance.hashCode() : 0;
	}
}
