package com.kte.backend.exception;

import org.springframework.stereotype.Component;


public class AuthenticationEntryPointException extends RuntimeException {
    public AuthenticationEntryPointException(String message) {
        super(message);
    }

    public AuthenticationEntryPointException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationEntryPointException(Throwable cause) {
        super(cause);
    }

    public AuthenticationEntryPointException(String message, Throwable cause,
                                             boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}