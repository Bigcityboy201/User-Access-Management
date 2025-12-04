package com.r2s.core.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.r2s.core.exception.UserAlreadyExistException;
import com.r2s.core.exception.UserNotFoundException;
import com.r2s.core.response.ErrorCode;
import com.r2s.core.response.ErrorResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handle UserNotFoundException should return 404 Not Found")
    void handleUserNotFoundException_shouldReturnNotFoundResponse() {
        // ===== ARRANGE =====
        UserNotFoundException exception = new UserNotFoundException("User not found");

        // ===== ACT =====
        ErrorResponse response = exceptionHandler.handle(exception);

        // ===== ASSERT =====
        assertNotNull(response);
        assertEquals(ErrorCode.NOT_FOUND, response.getCode());
        assertEquals("User not found", response.getMessage());
        assertEquals("user", response.getDomain());
    }

    @Test
    @DisplayName("handle AccessDeniedException should return 400 Bad Request with Access Denied")
    void handleAccessDeniedException_shouldReturnForbiddenResponse() {
        // ===== ARRANGE =====
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // ===== ACT =====
        ErrorResponse response = exceptionHandler.handleAuthorizationDenied(exception);

        // ===== ASSERT =====
        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST, response.getCode());
        assertEquals("Access Denied", response.getMessage());
        assertEquals("security", response.getDomain());
    }

    @Test
    @DisplayName("handle AuthorizationDeniedException should return 400 Bad Request with Access Denied")
    void handleAuthorizationDeniedException_shouldReturnForbiddenResponse() {
        // ===== ARRANGE =====
        AuthorizationResult authorizationResult = new AuthorizationDecision(false);
        AuthorizationDeniedException exception = new AuthorizationDeniedException("Denied", authorizationResult);

        // ===== ACT =====
        ErrorResponse response = exceptionHandler.handleAuthorizationDenied(exception);

        // ===== ASSERT =====
        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST, response.getCode());
        assertEquals("Access Denied", response.getMessage());
        assertEquals("security", response.getDomain());
    }

    @Test
    @DisplayName("handle BadCredentialsException should return 401 Unauthorized")
    void handleBadCredentialsException_shouldReturnUnauthorizedResponse() {
        // ===== ARRANGE =====
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // ===== ACT =====
        ErrorResponse response = exceptionHandler.handleBadCredentials(exception);

        // ===== ASSERT =====
        assertNotNull(response);
        assertEquals(ErrorCode.UNAUTHORIZED, response.getCode());
        assertEquals("Invalid username or password", response.getMessage());
        assertEquals("auth", response.getDomain());
    }

    @Test
    @DisplayName("handle generic Exception should return 500 Internal Server Error")
    void handleGenericException_shouldReturnInternalServerError() {
        // ===== ARRANGE =====
        Exception exception = new Exception("Generic error");

        // ===== ACT =====
        ResponseEntity<ErrorResponse> response = exceptionHandler.handle(exception);

        // ===== ASSERT =====
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ErrorCode.INTERNAL_SERVER, response.getBody().getCode());
        assertEquals("Generic error", response.getBody().getMessage());
        assertEquals("system", response.getBody().getDomain());
    }

    @Test
    @DisplayName("handle generic Exception with BadCredentials should return 401 Unauthorized")
    void handleGenericException_withBadCredentials_shouldReturnUnauthorized() {
        // ===== ARRANGE =====
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // ===== ACT =====
        ResponseEntity<ErrorResponse> response = exceptionHandler.handle(exception);

        // ===== ASSERT =====
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.UNAUTHORIZED, response.getBody().getCode());
        assertEquals("Invalid username or password", response.getBody().getMessage());
        assertEquals("auth", response.getBody().getDomain());
    }

    @Test
    @DisplayName("handle MethodArgumentNotValidException should return 400 Bad Request")
    void handleMethodArgumentNotValidException_shouldReturnBadRequest() {
        // ===== ARRANGE =====
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, new BindException(new Object(), "object"));
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        exception.getBindingResult().addError(fieldError);

        // ===== ACT =====
        ErrorResponse response = exceptionHandler.handleMethodArgumentNotValid(exception);

        // ===== ASSERT =====
        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST, response.getCode());
        assertEquals("Validation failed for object", response.getMessage());
        assertEquals("validation", response.getDomain());
    }

    @Test
    @DisplayName("handle ConstraintViolationException should return 400 Bad Request")
    void handleConstraintViolationException_shouldReturnBadRequest() {
        // ===== ARRANGE =====
        Set<ConstraintViolation<?>> violations = Collections.emptySet();
        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", violations);

        // ===== ACT =====
        ErrorResponse response = exceptionHandler.handleConstraintViolation(exception);

        // ===== ASSERT =====
        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST, response.getCode());
        assertEquals("Validation failed for constraint violation", response.getMessage());
        assertEquals("validation", response.getDomain());
    }

    @Test
    @DisplayName("handle IllegalArgumentException should return 400 Bad Request")
    void handleIllegalArgumentException_shouldReturnBadRequest() {
        // ===== ARRANGE =====
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // ===== ACT =====
        ErrorResponse response = exceptionHandler.handleIllegalArgument(exception);

        // ===== ASSERT =====
        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST, response.getCode());
        assertEquals("Invalid argument", response.getMessage());
        assertEquals("system", response.getDomain());
    }

    @Test
    @DisplayName("handle NullPointerException should return 500 Internal Server Error")
    void handleNullPointerException_shouldReturnInternalServerError() {
        // ===== ARRANGE =====
        NullPointerException exception = new NullPointerException("Null pointer");

        // ===== ACT =====
        ResponseEntity<ErrorResponse> response = exceptionHandler.handle(exception);

        // ===== ASSERT =====
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ErrorCode.INTERNAL_SERVER, response.getBody().getCode());
        assertEquals("Null pointer", response.getBody().getMessage());
        assertEquals("system", response.getBody().getDomain());
    }

    @Test
    @DisplayName("handle UserAlreadyExistException should return 409 Conflict")
    void handleUserAlreadyExistException_shouldReturnConflict() {
        // ===== ARRANGE =====
        UserAlreadyExistException exception = new UserAlreadyExistException("User exists");

        // ===== ACT =====
        ErrorResponse response = exceptionHandler.handle(exception);

        // ===== ASSERT =====
        assertNotNull(response);
        assertEquals(ErrorCode.CONFLICT, response.getCode());
        assertEquals("User exists", response.getMessage());
        assertEquals("user", response.getDomain());
    }
}
