package com.r2s.core.exception;

import com.r2s.core.response.ErrorCode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAlreadyExistException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final ErrorCode errorCode = ErrorCode.ALREADY_EXIT;
	private final String domain = "user";
	private String message;

	public UserAlreadyExistException(final String message) {
		this.message = message;
	}

}