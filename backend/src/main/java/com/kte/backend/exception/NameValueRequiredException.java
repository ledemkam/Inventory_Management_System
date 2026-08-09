package com.kte.backend.exception;

public class NameValueRequiredException extends RuntimeException {
    public NameValueRequiredException(String message) {
        super(message);
    }

    public NameValueRequiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public NameValueRequiredException(Throwable cause) {
        super(cause);
    }

    public NameValueRequiredException(String message, Throwable cause,
                                      boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}