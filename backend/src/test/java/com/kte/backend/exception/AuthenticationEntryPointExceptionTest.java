package com.kte.backend.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthenticationEntryPointException Tests")
class AuthenticationEntryPointExceptionTest {

    @Test
    @DisplayName("Should create exception with message only")
    void should_Create_With_Message() {
        final AuthenticationEntryPointException exception =
                new AuthenticationEntryPointException("JWT token is expired");

        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo("JWT token is expired");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void should_Create_With_Message_And_Cause() {
        final Throwable cause = new IllegalArgumentException("root cause");

        final AuthenticationEntryPointException exception =
                new AuthenticationEntryPointException("JWT token is expired", cause);

        assertThat(exception.getMessage()).isEqualTo("JWT token is expired");
        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    @DisplayName("Should create exception with cause only")
    void should_Create_With_Cause_Only() {
        final Throwable cause = new IllegalArgumentException("root cause");

        final AuthenticationEntryPointException exception = new AuthenticationEntryPointException(cause);

        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    @DisplayName("Should create exception with message, cause and suppression flags")
    void should_Create_With_All_Args() {
        final Throwable cause = new IllegalArgumentException("root cause");

        final AuthenticationEntryPointException exception =
                new AuthenticationEntryPointException("JWT token is expired", cause, false, false);

        assertThat(exception.getMessage()).isEqualTo("JWT token is expired");
        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getSuppressed()).isEmpty();
    }
}
