package com.kte.backend.exception.handler;

import com.kte.backend.exception.AccessDenieException;
import com.kte.backend.exception.AuthenticationEntryPointException;
import com.kte.backend.exception.EntityAlreadyExistsException;
import com.kte.backend.exception.Error;
import com.kte.backend.exception.InvalidCredentialsException;
import com.kte.backend.exception.NameValueRequiredException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler({
            AuthenticationException.class,
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<Error> handleAuthenticationException(Exception ex) {
        log.warn("Authentication failed: {}", ex.getMessage()); // log.warn, not log.error
        Error error = new Error();
        error.setError("Invalid credentials"); // Generic message for security
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Error> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        Error error = new Error();
        error.setError("Access denied");
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<Error> handleEntityAlreadyExists(
            EntityAlreadyExistsException ex
    ) {
        log.error("Caught EntityAlreadyExistsException", ex);
        Error error = new Error();
        error.setError(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Error> handleInvalidCredentials(
            InvalidCredentialsException ex
    ) {
        log.error("Caught InvalidCredentialsException", ex);
        Error error = new Error();
        error.setError(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NameValueRequiredException.class)
    public ResponseEntity<Error> handleNameValueRequired(
            NameValueRequiredException ex
    ) {
        log.error("Caught NameValueRequiredException", ex);
        Error error = new Error();
        error.setError(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDenieException.class)
    public ResponseEntity<Error> handleAccessDenied(
            AccessDenieException ex
    ) {
        log.error("Caught AccessDenieException", ex);
        Error error = new Error();
        error.setError(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationEntryPointException.class)
    public ResponseEntity<Error> handleAuthenticationEntryPoint(
            AuthenticationEntryPointException ex
    ) {
        log.error("Caught AuthenticationEntryPointException", ex);
        Error error = new Error();
        error.setError(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Error> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex
    ) {
        log.error("Caught MethodArgumentNotValidException", ex);
        Error error = new Error();

        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse("Validation error occurred");

        error.setError(errorMessage);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Error> handleConstraintViolation(
            ConstraintViolationException ex
    ) {
        log.error("Caught ConstraintViolationException", ex);
        Error error = new Error();

        String errorMessage = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation ->
                        violation.getPropertyPath() + ": " + violation.getMessage()
                ).orElse("Constraint violation occurred");

        error.setError(errorMessage);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> handleException(Exception ex) {
        log.error("Caught exception", ex);
        Error error = new Error();
        error.setError("An unknown error occurred");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
