package org.throwable.rabbitmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.throwable.rabbitmq.annotation.EnableAmqpRabbitmq;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 21:38
 */@SpringBootApplication
@EnableAmqpRabbitmq
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
