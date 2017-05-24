package org.throwable.exception;

/**
 * @author throwable
 * @version v1.0
 * @since 2017/3/30 12:45
 * @description 不支持元素异常
 */
public class UnsupportedElementException extends RuntimeException {

	public UnsupportedElementException(String message) {
		super(message);
	}

	public UnsupportedElementException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedElementException(Throwable cause) {
		super(cause);
	}
}
