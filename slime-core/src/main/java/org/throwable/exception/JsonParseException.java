package org.throwable.exception;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/5/21 14:39
 */
public class JsonParseException extends RuntimeException {

	public JsonParseException(String message) {
		super(message);
	}

	public JsonParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public JsonParseException(Throwable cause) {
		super(cause);
	}
}
