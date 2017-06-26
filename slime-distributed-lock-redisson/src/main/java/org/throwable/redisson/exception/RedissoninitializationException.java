package org.throwable.redisson.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/6/25 2:23
 */
public class RedissoninitializationException extends RuntimeException {

	public RedissoninitializationException(String message) {
		super(message);
	}

	public RedissoninitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public RedissoninitializationException(Throwable cause) {
		super(cause);
	}
}
