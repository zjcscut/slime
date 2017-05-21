package org.throwable.rabbitmq.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 15:15
 */
public class RabbitmqInstanceCreationException extends RuntimeException {

	public RabbitmqInstanceCreationException(String message) {
		super(message);
	}

	public RabbitmqInstanceCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public RabbitmqInstanceCreationException(Throwable cause) {
		super(cause);
	}
}
