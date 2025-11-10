package com.r2s.core.handler;

import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.r2s.core.exception.UserNotFoundException;
import com.r2s.core.response.ErrorCode;
import com.r2s.core.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	@ExceptionHandler({ UserNotFoundException.class })
	public ErrorResponse handle(final UserNotFoundException ex) {
		return ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), ex.getDomain());
	}

	@ResponseStatus(value = HttpStatus.FORBIDDEN)
	@ExceptionHandler({ AuthorizationDeniedException.class, AccessDeniedException.class })
	public ErrorResponse handleAuthorizationDenied(final Exception exception) {
		return ErrorResponse.of(ErrorCode.BAD_REQUEST, "Access Denied", "security");
	}

	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({ Exception.class })
	public ErrorResponse handle(final Exception ex) {
		return ErrorResponse.of(ErrorCode.INTERNAL_SERVER, ex.getMessage(), "system");
	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler({ MethodArgumentNotValidException.class })
	public ErrorResponse handle(final MethodArgumentNotValidException exception) {
		Map<String, Object> details = exception.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(x -> x.getField().toString(), x -> x.getDefaultMessage(), (a, b) -> b));

		return ErrorResponse.of(ErrorCode.INTERNAL_SERVER, "Validation failed", "request", details);

	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler({ ConstraintViolationException.class })
	public ErrorResponse handle(final ConstraintViolationException exception) {
		Map<String, Object> details = exception.getConstraintViolations().stream()
				.collect(Collectors.toMap(x -> x.getPropertyPath().toString(), x -> x.getMessage(), (a, b) -> b));

		return ErrorResponse.of(ErrorCode.BAD_REQUEST, "Validation failed", "request", details);
	}
}
