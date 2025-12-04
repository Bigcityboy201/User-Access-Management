package com.r2s.core.handler;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.r2s.core.exception.UserAlreadyExistException;
import com.r2s.core.exception.UserNotFoundException;
import com.r2s.core.response.ErrorCode;
import com.r2s.core.response.ErrorResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	@ExceptionHandler({ UserNotFoundException.class })
	public ErrorResponse handle(final UserNotFoundException ex) {
		return ErrorResponse.of(ex.getErrorCode(), ex.getDomain(), ex.getMessage());
	}

	@ResponseStatus(value = HttpStatus.CONFLICT)
	@ExceptionHandler({ UserAlreadyExistException.class })
	public ErrorResponse handle(final UserAlreadyExistException ex) {
		return ErrorResponse.of(ErrorCode.CONFLICT, ex.getDomain(), ex.getMessage());
	}

	@ResponseStatus(value = HttpStatus.FORBIDDEN)
	@ExceptionHandler({ AuthorizationDeniedException.class, AccessDeniedException.class })
	public ErrorResponse handleAuthorizationDenied(final Exception exception) {
		return ErrorResponse.of(ErrorCode.BAD_REQUEST, "security", "Access Denied");
	}

	@ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleBadCredentials(Exception ex) {
		return ErrorResponse.of(ErrorCode.UNAUTHORIZED, "auth", "Invalid username or password");
	}

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<ErrorResponse> handle(final Exception ex) {
		// Handle BadCredentialsException here since Spring is matching this handler first
		if (ex instanceof BadCredentialsException) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ErrorResponse.of(ErrorCode.UNAUTHORIZED, "auth", "Invalid username or password"));
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER, "system", ex.getMessage()));
	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler({ MethodArgumentNotValidException.class })
	public ErrorResponse handleMethodArgumentNotValid(final MethodArgumentNotValidException exception) {
		Map<String, Object> details = exception.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> b));

		String objectName = exception.getBindingResult().getObjectName();
		// Determine message:
		// - For generic binding scenarios (like in core tests) keep the old format
		//   "Validation failed for {objectName}"
		// - For real request DTOs (e.g. UpdateUserRequest) prefer the specific field
		//   error message so controllers can assert on it
		String firstFieldMessage = exception.getBindingResult().getFieldErrors().stream().findFirst()
				.map(FieldError::getDefaultMessage).orElse(null);

		String message;
		if ("object".equals(objectName)) {
			message = "Validation failed for " + objectName;
		} else if (firstFieldMessage != null) {
			message = firstFieldMessage;
		} else {
			message = "Validation failed for " + objectName;
		}

		return ErrorResponse.of(ErrorCode.BAD_REQUEST, "validation", message, details);
	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler({ ConstraintViolationException.class })
	public ErrorResponse handleConstraintViolation(final ConstraintViolationException exception) {
		Map<String, Object> details = exception.getConstraintViolations().stream()
				.collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(), ConstraintViolation::getMessage, (a, b) -> b));

		return ErrorResponse.of(ErrorCode.BAD_REQUEST, "validation", "Validation failed for constraint violation", details);
	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler({ IllegalArgumentException.class })
	public ErrorResponse handleIllegalArgument(final IllegalArgumentException exception) {
		return ErrorResponse.of(ErrorCode.BAD_REQUEST, "system", exception.getMessage());
	}
}
