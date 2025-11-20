package com.r2s.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.security.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

	private JwtUtils jwtUtils;
	private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
	private static final long TEST_DURATION = 86400L; // 24 hours

	@BeforeEach
	void setUp() {
		jwtUtils = new JwtUtils();
		ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
		ReflectionTestUtils.setField(jwtUtils, "jwtDuration", TEST_DURATION);
	}

	@Test
	void generateToken_shouldCreateValidToken() {
		// Setup
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User user = User.builder().id(1).username("testuser").password("password")
				.roles(List.of(userRole)).build();
		CustomUserDetails userDetails = new CustomUserDetails(user);

		// Execute
		String token = jwtUtils.generateToken(userDetails);

		// Verify
		assertNotNull(token);
		assertFalse(token.isEmpty());
	}

	@Test
	void extractUsername_shouldExtractUsernameFromToken() {
		// Setup
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User user = User.builder().id(1).username("testuser").password("password")
				.roles(List.of(userRole)).build();
		CustomUserDetails userDetails = new CustomUserDetails(user);
		String token = jwtUtils.generateToken(userDetails);

		// Execute
		String username = jwtUtils.extractUsername(token);

		// Verify
		assertEquals("testuser", username);
	}

	@Test
	void extractExpiration_shouldExtractExpirationDate() {
		// Setup
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User user = User.builder().id(1).username("testuser").password("password")
				.roles(List.of(userRole)).build();
		CustomUserDetails userDetails = new CustomUserDetails(user);
		String token = jwtUtils.generateToken(userDetails);

		// Execute
		Date expiration = jwtUtils.extractExpiration(token);

		// Verify
		assertNotNull(expiration);
		assertTrue(expiration.after(new Date()));
	}

	@Test
	void isTokenExpired_shouldReturnFalseForValidToken() {
		// Setup
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User user = User.builder().id(1).username("testuser").password("password")
				.roles(List.of(userRole)).build();
		CustomUserDetails userDetails = new CustomUserDetails(user);
		String token = jwtUtils.generateToken(userDetails);

		// Execute
		boolean expired = jwtUtils.isTokenExpired(token);

		// Verify
		assertFalse(expired);
	}

	@Test
	void validateToken_shouldReturnTrueForValidToken() {
		// Setup
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User user = User.builder().id(1).username("testuser").password("password")
				.roles(List.of(userRole)).build();
		CustomUserDetails userDetails = new CustomUserDetails(user);
		String token = jwtUtils.generateToken(userDetails);

		// Execute
		boolean valid = jwtUtils.validateToken(token);

		// Verify
		assertTrue(valid);
	}

	@Test
	void validateToken_shouldReturnFalseForInvalidToken() {
		// Setup
		String invalidToken = "invalid.token.here";

		// Execute
		boolean valid = jwtUtils.validateToken(invalidToken);

		// Verify
		assertFalse(valid);
	}

	@Test
	void validateToken_shouldReturnFalseForExpiredToken() {
		// Setup - Create token with very short expiration
		JwtUtils shortLivedJwtUtils = new JwtUtils();
		ReflectionTestUtils.setField(shortLivedJwtUtils, "jwtSecret", TEST_SECRET);
		ReflectionTestUtils.setField(shortLivedJwtUtils, "jwtDuration", -3600L); // Negative duration = expired

		Role userRole = Role.builder().id(1).roleName("USER").build();
		User user = User.builder().id(1).username("testuser").password("password")
				.roles(List.of(userRole)).build();
		CustomUserDetails userDetails = new CustomUserDetails(user);
		String expiredToken = shortLivedJwtUtils.generateToken(userDetails);

		// Wait a bit to ensure expiration
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// Execute
		boolean valid = jwtUtils.validateToken(expiredToken);

		// Verify
		assertFalse(valid);
	}
}

