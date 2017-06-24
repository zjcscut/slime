package org.throwable.rabbitmq.support;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.connection.SimpleResourceHolder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.throwable.rabbitmq.Application;
import org.throwable.rabbitmq.configuration.RabbitmqProperties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 21:38
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class RabbitInstanceRegistrarTest {

	@Autowired
	private RabbitmqProperties rabbitmqProperties;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Test
	public void testRegiesterRabbitmqComponent() throws Exception {
		System.out.println("success");

		SimpleResourceHolder.bind(rabbitTemplate.getConnectionFactory(), "PRODUCER-1");
		try {
			rabbitTemplate.convertAndSend("exchange1", "queue-key-1", "hello world!!!");
		} finally {
			SimpleResourceHolder.unbind(rabbitTemplate.getConnectionFactory());
		}
		Thread.sleep(Integer.MAX_VALUE);
	}

}