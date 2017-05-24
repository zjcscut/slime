package org.throwable.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/4/2 14:14
 */
public class BeanReflectionException extends RuntimeException {

	public BeanReflectionException(String message) {
		super(message);
	}

	public BeanReflectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public BeanReflectionException(Throwable cause) {
		super(cause);
	}
}
