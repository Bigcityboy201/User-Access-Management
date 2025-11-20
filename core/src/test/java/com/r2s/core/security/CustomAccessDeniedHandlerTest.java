package com.r2s.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
	void handle_shouldSetForbiddenStatus() throws IOException, ServletException {
		// Setup
		AccessDeniedException exception = new AccessDeniedException("Access denied");

		// Execute
		accessDeniedHandler.handle(request, response, exception);

		// Verify
		assertEquals(403, response.getStatus());
	}

	@Test
	void handle_shouldSetJsonContentType() throws IOException, ServletException {
		// Setup
		AccessDeniedException exception = new AccessDeniedException("Access denied");

		// Execute
		accessDeniedHandler.handle(request, response, exception);

		// Verify
		assertEquals("application/json;charset=UTF-8", response.getContentType());
	}

	@Test
	void handle_shouldWriteErrorResponse() throws IOException, ServletException {
		// Setup
		AccessDeniedException exception = new AccessDeniedException("Access denied");

		// Execute
		accessDeniedHandler.handle(request, response, exception);

		// Verify
		String responseBody = response.getContentAsString();
		assertEquals("{\"error\":\"Access Denied\",\"message\":\"You do not have permission to access this resource\"}",
				responseBody);
	}
}

