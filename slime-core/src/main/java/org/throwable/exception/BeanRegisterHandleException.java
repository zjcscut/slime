package org.throwable.exception;

/**
 * @author throwable
 * @version v1.0
 * @since 2017/3/29 0:17
 * @description Bean注册处理异常
 */
public class BeanRegisterHandleException extends RuntimeException {

	public BeanRegisterHandleException(String message) {
		super(message);
	}

	public BeanRegisterHandleException(String message, Throwable cause) {
		super(message, cause);
	}

	public BeanRegisterHandleException(Throwable cause) {
		super(cause);
	}
}
