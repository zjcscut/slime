package org.throwable.rabbitmq.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 21:08
 */
public class RabbitmqRegisterException extends RuntimeException {

	public RabbitmqRegisterException(String message) {
		super(message);
	}

	public RabbitmqRegisterException(String message, Throwable cause) {
		super(message, cause);
	}

	public RabbitmqRegisterException(Throwable cause) {
		super(cause);
	}
}
