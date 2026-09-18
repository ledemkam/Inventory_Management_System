package com.kte.backend.exception.handler;

import com.kte.backend.exception.AccessDenieException;
import com.kte.backend.exception.AuthenticationEntryPointException;
import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.exception.Error;
import com.kte.backend.exception.InvalidCredentialsException;
import com.kte.backend.exception.NameValueRequiredException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Should return 401 with generic message for authentication failures")
    void handleAuthenticationException() {
        final ResponseEntity<Error> response =
                handler.handleAuthenticationException(new BadCredentialsException("bad password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    @DisplayName("Should return 401 with generic message for unknown username")
    void handleAuthenticationException_UsernameNotFound() {
        final ResponseEntity<Error> response =
                handler.handleAuthenticationException(new UsernameNotFoundException("unknown"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    @DisplayName("Should return 403 for Spring Security AccessDeniedException")
    void handleAccessDenied() {
        final ResponseEntity<Error> response =
                handler.handleAccessDenied(new AccessDeniedException("no rights"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Access denied");
    }

    @Test
    @DisplayName("Should return 409 when entity already exists")
    void handleEntityAlreadyExists() {
        final ResponseEntity<Error> response =
                handler.handleEntityAlreadyExists(new EntityAlreadyExistsException("Product already exists"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Product already exists");
    }

    @Test
    @DisplayName("Should return 401 for invalid credentials")
    void handleInvalidCredentials() {
        final ResponseEntity<Error> response =
                handler.handleInvalidCredentials(new InvalidCredentialsException("Wrong password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Wrong password");
    }

    @Test
    @DisplayName("Should return 400 when a required name/value is missing")
    void handleNameValueRequired() {
        final ResponseEntity<Error> response =
                handler.handleNameValueRequired(new NameValueRequiredException("Name is required"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Name is required");
    }

    @Test
    @DisplayName("Should return 403 for domain-level AccessDenieException")
    void testHandleAccessDenied() {
        final ResponseEntity<Error> response =
                handler.handleAccessDenied(new AccessDenieException("Not allowed to edit this transaction"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Not allowed to edit this transaction");
    }

    @Test
    @DisplayName("Should return 401 for authentication entry point failures")
    void handleAuthenticationEntryPoint() {
        final ResponseEntity<Error> response =
                handler.handleAuthenticationEntryPoint(new AuthenticationEntryPointException("JWT token is expired"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("JWT token is expired");
    }

    @Test
    @DisplayName("Should return 400 with the first field error when validation fails")
    void handleMethodArgumentNotValidException() throws NoSuchMethodException {
        final BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "must not be blank"));

        final ResponseEntity<Error> response = handler.handleMethodArgumentNotValidException(
                new MethodArgumentNotValidException(dummyMethodParameter(), bindingResult));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("name: must not be blank");
    }

    @Test
    @DisplayName("Should fall back to a generic message when validation fails without field errors")
    void handleMethodArgumentNotValidException_NoFieldErrors() throws NoSuchMethodException {
        final BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");

        final ResponseEntity<Error> response = handler.handleMethodArgumentNotValidException(
                new MethodArgumentNotValidException(dummyMethodParameter(), bindingResult));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation error occurred");
    }

    private MethodParameter dummyMethodParameter() throws NoSuchMethodException {
        final Method method = getClass().getDeclaredMethod("dummyTarget", String.class);
        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    private void dummyTarget(final String name) {
        // Used only via reflection to build a real MethodParameter for MethodArgumentNotValidException.
    }

    @Test
    @DisplayName("Should return 400 with the first constraint violation")
    void handleConstraintViolation() {
        final ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        final Path path = mock(Path.class);
        when(path.toString()).thenReturn("quantity");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be positive");

        final ResponseEntity<Error> response =
                handler.handleConstraintViolation(new ConstraintViolationException(Set.of(violation)));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("quantity: must be positive");
    }

    @Test
    @DisplayName("Should fall back to a generic message when there are no constraint violations")
    void handleConstraintViolation_NoViolations() {
        final ResponseEntity<Error> response =
                handler.handleConstraintViolation(new ConstraintViolationException(Set.of()));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Constraint violation occurred");
    }

    @Test
    @DisplayName("Should return 500 for any unhandled exception")
    void handleException() {
        final ResponseEntity<Error> response = handler.handleException(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An unknown error occurred");
    }
}
