package com.r2s.core.exception;

import com.r2s.core.response.ErrorCode;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final ErrorCode errorCode = ErrorCode.NOT_FOUND;
	private final String domain = "user";// thay domain=entity tên hợp lí
	private String message;

	public UserNotFoundException(final String message) {
		this.message = message;
	}
}
