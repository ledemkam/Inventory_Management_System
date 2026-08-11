package com.kte.backend.exception;

public class AccessDenieException extends RuntimeException {
    public AccessDenieException(String message) {
        super(message);
    }

    public AccessDenieException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessDenieException(Throwable cause) {
        super(cause);
    }

    public AccessDenieException(String message, Throwable cause,
                                boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}