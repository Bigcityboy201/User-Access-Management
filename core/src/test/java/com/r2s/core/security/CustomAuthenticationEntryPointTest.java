package com.r2s.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

	private CustomAuthenticationEntryPoint authenticationEntryPoint;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		authenticationEntryPoint = new CustomAuthenticationEntryPoint();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	void commence_shouldSetUnauthorizedStatus() throws IOException {
		// Setup
		BadCredentialsException exception = new BadCredentialsException("Bad credentials");

		// Execute
		authenticationEntryPoint.commence(request, response, exception);

		// Verify
		assertEquals(401, response.getStatus());
	}

	@Test
	void commence_shouldSetJsonContentType() throws IOException {
		// Setup
		BadCredentialsException exception = new BadCredentialsException("Bad credentials");

		// Execute
		authenticationEntryPoint.commence(request, response, exception);

		// Verify
		assertEquals("application/json;charset=UTF-8", response.getContentType());
	}

	@Test
	void commence_shouldWriteErrorResponse() throws IOException {
		// Setup
		BadCredentialsException exception = new BadCredentialsException("Bad credentials");

		// Execute
		authenticationEntryPoint.commence(request, response, exception);

		// Verify
		String responseBody = response.getContentAsString();
		assertEquals(
				"{\"error\":\"Unauthorized\",\"message\":\"Authentication required. Please login to access this resource.\"}",
				responseBody);
	}
}

