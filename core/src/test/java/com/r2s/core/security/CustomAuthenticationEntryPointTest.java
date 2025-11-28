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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

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
    @DisplayName("commence should set 401 Unauthorized status")
    void commence_shouldSetUnauthorizedStatus() throws IOException {
        // ===== ARRANGE =====
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // ===== ACT =====
        authenticationEntryPoint.commence(request, response, exception);

        // ===== ASSERT =====
        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("commence should set JSON content type")
    void commence_shouldSetJsonContentType() throws IOException {
        // ===== ARRANGE =====
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // ===== ACT =====
        authenticationEntryPoint.commence(request, response, exception);

        // ===== ASSERT =====
        assertEquals("application/json;charset=UTF-8", response.getContentType());
    }

    @Test
    @DisplayName("commence should write expected JSON error response")
    void commence_shouldWriteErrorResponse() throws IOException {
        // ===== ARRANGE =====
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // ===== ACT =====
        authenticationEntryPoint.commence(request, response, exception);

        // ===== ASSERT =====
        String responseBody = response.getContentAsString();
        assertEquals(
                "{\"error\":\"Unauthorized\",\"message\":\"Authentication required. Please login to access this resource.\"}",
                responseBody);
    }

    @Test
    @DisplayName("commence should handle null exception gracefully")
    void commence_shouldHandleNullException() throws IOException {
        // ===== ARRANGE =====
        AuthenticationException exception = null;

        // ===== ACT =====
        authenticationEntryPoint.commence(request, response, exception);

        // ===== ASSERT =====
        assertEquals(401, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        String responseBody = response.getContentAsString();
        assertEquals(
                "{\"error\":\"Unauthorized\",\"message\":\"Authentication required. Please login to access this resource.\"}",
                responseBody);
    }

    @Test
    @DisplayName("commence should handle different types of AuthenticationException")
    void commence_shouldHandleDifferentExceptionTypes() throws IOException {
        // ===== ARRANGE =====
        AuthenticationException exception = new AuthenticationException("Custom auth exception") {};

        // ===== ACT =====
        authenticationEntryPoint.commence(request, response, exception);

        // ===== ASSERT =====
        assertEquals(401, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        String responseBody = response.getContentAsString();
        assertEquals(
                "{\"error\":\"Unauthorized\",\"message\":\"Authentication required. Please login to access this resource.\"}",
                responseBody);
    }
}
