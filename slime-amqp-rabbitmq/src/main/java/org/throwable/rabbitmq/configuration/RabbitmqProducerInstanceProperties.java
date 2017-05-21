package org.throwable.rabbitmq.configuration;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 17:52
 */
public class RabbitmqProducerInstanceProperties extends RabbitmqInstanceProperties{

	protected Boolean useConfirmCallback;
	protected Boolean mandatory = false;
	protected Boolean useReturnCallback;  //当 useReturnCallback = true,mandatory必须为true

	public RabbitmqProducerInstanceProperties() {
	}

	public Boolean getUseConfirmCallback() {
		return useConfirmCallback;
	}

	public void setUseConfirmCallback(Boolean useConfirmCallback) {
		this.useConfirmCallback = useConfirmCallback;
	}

	public Boolean getMandatory() {
		return mandatory;
	}

	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}

	public Boolean getUseReturnCallback() {
		return useReturnCallback;
	}

	public void setUseReturnCallback(Boolean useReturnCallback) {
		this.useReturnCallback = useReturnCallback;
	}
}
