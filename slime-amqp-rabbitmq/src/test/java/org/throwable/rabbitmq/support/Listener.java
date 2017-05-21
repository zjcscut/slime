package org.throwable.rabbitmq.support;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 21:16
 */
@Component
public class Listener implements MessageListener {

	@Override
	public void onMessage(Message message) {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("receive message --> " + new String(message.getBody()));
	}
}
