package com.r2s.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

	private CustomAccessDeniedHandler accessDeniedHandler;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		accessDeniedHandler = new CustomAccessDeniedHandler();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	@DisplayName("handle should set 403 Forbidden status")
	void handle_shouldSetForbiddenStatus() throws IOException, ServletException {
		// ===== ARRANGE =====
		AccessDeniedException exception = new AccessDeniedException("Access denied");

		// ===== ACT =====
		accessDeniedHandler.handle(request, response, exception);

		// ===== ASSERT =====
		assertEquals(403, response.getStatus());
	}

	@Test
	@DisplayName("handle should set JSON content type")
	void handle_shouldSetJsonContentType() throws IOException, ServletException {
		// ===== ARRANGE =====
		AccessDeniedException exception = new AccessDeniedException("Access denied");

		// ===== ACT =====
		accessDeniedHandler.handle(request, response, exception);

		// ===== ASSERT =====
		assertEquals("application/json;charset=UTF-8", response.getContentType());
	}

	@Test
	@DisplayName("handle should write expected JSON error response")
	void handle_shouldWriteErrorResponse() throws IOException, ServletException {
		// ===== ARRANGE =====
		AccessDeniedException exception = new AccessDeniedException("Access denied");

		// ===== ACT =====
		accessDeniedHandler.handle(request, response, exception);

		// ===== ASSERT =====
		String responseBody = response.getContentAsString();
		assertEquals("{\"error\":\"Access Denied\",\"message\":\"You do not have permission to access this resource\"}",
				responseBody);
	}

	//
	@Test
	@DisplayName("handle should handle null exception gracefully")
	void handle_shouldHandleNullException() throws IOException, ServletException {
		// ===== ARRANGE =====
		AccessDeniedException exception = null;

		// ===== ACT =====
		accessDeniedHandler.handle(request, response, exception);

		// ===== ASSERT =====
		assertEquals(403, response.getStatus());
		assertEquals("application/json;charset=UTF-8", response.getContentType());
		String responseBody = response.getContentAsString();
		assertEquals("{\"error\":\"Access Denied\",\"message\":\"You do not have permission to access this resource\"}",
				responseBody);
	}

	//
	@Test
	@DisplayName("handle should set correct error code in JSON response")
	void handle_shouldSetCorrectErrorCode() throws IOException, ServletException {
		// ===== ARRANGE =====
		AccessDeniedException exception = new AccessDeniedException("Access denied");

		// ===== ACT =====
		accessDeniedHandler.handle(request, response, exception);

		// ===== ASSERT =====
		String responseBody = response.getContentAsString();
		assertEquals(true, responseBody.contains("\"error\":\"Access Denied\""));
	}
}
