package com.r2s.core.security;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.util.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	private JwtUtils jwtUtils;

	@Mock
	private UserDetailsService userDetailsService;

	@Mock
	private FilterChain filterChain;

	@InjectMocks
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@BeforeEach
	void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		SecurityContextHolder.clearContext();
	}

	@Test
	void doFilterInternal_shouldSetAuthenticationWhenValidToken() throws ServletException, IOException {
		// Setup
		String token = "valid-token";
		request.addHeader("Authorization", "Bearer " + token);

		Role userRole = Role.builder().id(1).roleName("USER").build();
		User user = User.builder().id(1).username("testuser").password("password")
				.roles(new ArrayList<>(List.of(userRole))).build();
		UserDetails userDetails = new CustomUserDetails(user);

		when(jwtUtils.validateToken(token)).thenReturn(true);
		when(jwtUtils.extractUsername(token)).thenReturn("testuser");
		when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

		// Execute
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// Verify
		verify(jwtUtils).validateToken(token);
		verify(jwtUtils).extractUsername(token);
		verify(userDetailsService).loadUserByUsername("testuser");
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilterInternal_shouldNotSetAuthenticationWhenNoToken() throws ServletException, IOException {
		// Setup - No Authorization header

		// Execute
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// Verify
		verify(jwtUtils, never()).validateToken(anyString());
		verify(userDetailsService, never()).loadUserByUsername(anyString());
		verify(filterChain).doFilter(request, response);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void doFilterInternal_shouldNotSetAuthenticationWhenInvalidToken() throws ServletException, IOException {
		// Setup
		String token = "invalid-token";
		request.addHeader("Authorization", "Bearer " + token);

		when(jwtUtils.validateToken(token)).thenReturn(false);

		// Execute
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// Verify
		verify(jwtUtils).validateToken(token);
		verify(jwtUtils, never()).extractUsername(anyString());
		verify(userDetailsService, never()).loadUserByUsername(anyString());
		verify(filterChain).doFilter(request, response);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void doFilterInternal_shouldNotSetAuthenticationWhenTokenWithoutBearerPrefix() throws ServletException, IOException {
		// Setup
		request.addHeader("Authorization", "invalid-token");

		// Execute
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// Verify
		verify(jwtUtils, never()).validateToken(anyString());
		verify(userDetailsService, never()).loadUserByUsername(anyString());
		verify(filterChain).doFilter(request, response);
	}
}

