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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Test")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Test handling of EntityAlreadyExistsException")
    void handleEntityAlreadyExists() {
        //Given
        EntityAlreadyExistsException ex = new EntityAlreadyExistsException("Entity already exists");

        //When
        ResponseEntity<Error> response = globalExceptionHandler.handleEntityAlreadyExists(ex);

        //Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Entity already exists", response.getBody().getError());
    }

    @Test
    @DisplayName("Test handling of InvalidCredentialsException")
    void handleInvalidCredentials() {
        //Given
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");

        //When
        ResponseEntity<Error> response = globalExceptionHandler.handleInvalidCredentials(ex);

        //Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().getError());
    }

    @Test
    @DisplayName("Test handling of NameValueRequiredException")
    void handleNameValueRequired() {
        //Given
        NameValueRequiredException ex = new NameValueRequiredException("Name value is required");

        //When
        ResponseEntity<Error> response = globalExceptionHandler.handleNameValueRequired(ex);

        //Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Name value is required", response.getBody().getError());
    }

    @Test
    @DisplayName("Test handling of AccessDenieException")
    void handleAccessDenied() {
        //Given
        AccessDenieException ex = new AccessDenieException("Access denied");

        //When
        ResponseEntity<Error> response = globalExceptionHandler.handleAccessDenied(ex);

        //Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access denied", response.getBody().getError());
    }

    @Test
    @DisplayName("Test handling of AuthenticationEntryPointException")
    void handleAuthenticationEntryPoint() {
        //Given
        AuthenticationEntryPointException ex = new AuthenticationEntryPointException("Authentication required");

        //When
        ResponseEntity<Error> response = globalExceptionHandler.handleAuthenticationEntryPoint(ex);

        //Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Authentication required", response.getBody().getError());
    }

    @Test
    @DisplayName("Test handling of MethodArgumentNotValidException")
    void handleMethodArgumentNotValidException() {
        //Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "name", "Name is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        //When
        ResponseEntity<Error> response = globalExceptionHandler.handleMethodArgumentNotValidException(ex);

        //Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("name: Name is required", response.getBody().getError());
    }

    @Test
    @DisplayName("Test handling of MethodArgumentNotValidException with no field errors")
    void handleMethodArgumentNotValidException_NoFieldErrors() {
        //Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        //When
        ResponseEntity<Error> response = globalExceptionHandler.handleMethodArgumentNotValidException(ex);

        //Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation error occurred", response.getBody().getError());
    }

    @Test
    @DisplayName("Test handling of ConstraintViolationException")
    void handleConstraintViolation() {
        //Given
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("quantity");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be positive");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        //When
        ResponseEntity<Error> response = globalExceptionHandler.handleConstraintViolation(ex);

        //Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("quantity: must be positive", response.getBody().getError());
    }

    @Test
    @DisplayName("Test handling of ConstraintViolationException with no violations")
    void handleConstraintViolation_NoViolations() {
        //Given
        ConstraintViolationException ex = new ConstraintViolationException(Set.of());

        //When
        ResponseEntity<Error> response = globalExceptionHandler.handleConstraintViolation(ex);

        //Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Constraint violation occurred", response.getBody().getError());
    }

    @Test
    @DisplayName("Test handling of generic Exception")
    void handleException() {
        //Given
        Exception ex = new RuntimeException("Unexpected failure");

        //When
        ResponseEntity<Error> response = globalExceptionHandler.handleException(ex);

        //Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unknown error occurred", response.getBody().getError());
    }
}