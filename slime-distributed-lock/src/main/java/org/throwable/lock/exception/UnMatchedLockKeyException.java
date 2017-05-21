package org.throwable.lock.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/18 1:45
 */
public class UnMatchedLockKeyException extends RuntimeException {

	public UnMatchedLockKeyException(String message) {
		super(message);
	}

	public UnMatchedLockKeyException(String message, Throwable cause) {
		super(message, cause);
	}
}
