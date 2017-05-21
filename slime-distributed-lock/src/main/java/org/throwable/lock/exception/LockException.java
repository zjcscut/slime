package org.throwable.lock.exception;

/**
 * @author throwable
 * @version v1.0
 * @function
 * @since 2017/5/17 11:57
 */
public class LockException extends RuntimeException {

    public LockException(String message) {
        super(message);
    }

    public LockException(Throwable cause) {
        super(cause);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }
}
