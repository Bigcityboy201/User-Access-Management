package com.r2s.core.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.r2s.core.exception.UserNotFoundException;
import com.r2s.core.response.ErrorCode;
import com.r2s.core.response.ErrorResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

class GlobalExceptionHandlerTest {

	private GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

	@Test
	void handleUserNotFoundException_shouldReturnNotFoundResponse() {
		// Setup
		UserNotFoundException exception = new UserNotFoundException("User not found");

		// Execute
		ErrorResponse response = exceptionHandler.handle(exception);

		// Verify
		assertNotNull(response);
		assertEquals(ErrorCode.NOT_FOUND, response.getCode());
		assertEquals("User not found", response.getMessage());
		assertEquals("user", response.getDomain());
	}

	@Test
	void handleAccessDeniedException_shouldReturnForbiddenResponse() {
		// Setup
		AccessDeniedException exception = new AccessDeniedException("Access denied");

		// Execute
		ErrorResponse response = exceptionHandler.handleAuthorizationDenied(exception);

		// Verify
		assertNotNull(response);
		assertEquals(ErrorCode.BAD_REQUEST, response.getCode());
		assertEquals("Access Denied", response.getMessage());
		assertEquals("security", response.getDomain());
	}

	@Test
	void handleAuthorizationDeniedException_shouldReturnForbiddenResponse() {
		// Setup
		AuthorizationResult authorizationResult = new AuthorizationDecision(false);
		AuthorizationDeniedException exception = new AuthorizationDeniedException("Denied", authorizationResult);

		// Execute
		ErrorResponse response = exceptionHandler.handleAuthorizationDenied(exception);

		// Verify
		assertNotNull(response);
		assertEquals(ErrorCode.BAD_REQUEST, response.getCode());
		assertEquals("Access Denied", response.getMessage());
		assertEquals("security", response.getDomain());
	}

	@Test
	void handleBadCredentialsException_shouldReturnUnauthorizedResponse() {
		// Setup
		BadCredentialsException exception = new BadCredentialsException("Bad credentials");

		// Execute
		ErrorResponse response = exceptionHandler.handleBadCredentials(exception);

		// Verify
		assertNotNull(response);
		assertEquals(ErrorCode.UNAUTHORIZED, response.getCode());
		assertEquals("Invalid username or password", response.getMessage());
		assertEquals("auth", response.getDomain());
	}

	@Test
	void handleGenericException_shouldReturnInternalServerError() {
		// Setup
		Exception exception = new Exception("Generic error");

		// Execute
		ResponseEntity<ErrorResponse> response = exceptionHandler.handle(exception);

		// Verify
		assertNotNull(response);
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertEquals(ErrorCode.INTERNAL_SERVER, response.getBody().getCode());
		assertEquals("Generic error", response.getBody().getMessage());
		assertEquals("system", response.getBody().getDomain());
	}

	@Test
	void handleGenericException_withBadCredentials_shouldReturnUnauthorized() {
		// Setup
		BadCredentialsException exception = new BadCredentialsException("Bad credentials");

		// Execute
		ResponseEntity<ErrorResponse> response = exceptionHandler.handle(exception);

		// Verify
		assertNotNull(response);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertEquals(ErrorCode.UNAUTHORIZED, response.getBody().getCode());
		assertEquals("Invalid username or password", response.getBody().getMessage());
		assertEquals("auth", response.getBody().getDomain());
	}
}

