package org.throwable.rabbitmq.support;

import org.springframework.amqp.rabbit.support.CorrelationData;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/24 18:47
 */
public abstract class AbstractRabbitConfirmCallback extends RabbitConfirmCallbackListener{

	@Override
	public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack){
        	processForAck(correlationData, true, cause);
		}else {
			processForNack(correlationData, false, cause);
		}
	}

	protected abstract void processForAck(CorrelationData correlationData, boolean ack, String cause);

	protected abstract void processForNack(CorrelationData correlationData, boolean ack, String cause);

}
