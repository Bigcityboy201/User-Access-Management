package com.r2s.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;

class CustomUserDetailsTest {

	@Test
	@DisplayName("Constructor should create user details with roles")
	void constructor_shouldCreateUserDetailsWithRoles() {
		// ===== ARRANGE =====
		Role userRole = Role.builder().id(1).roleName("USER").build();
		Role adminRole = Role.builder().id(2).roleName("ADMIN").build();
		User user = User.builder().id(1).username("testuser").password("password").fullname("Test User")
				.email("test@example.com").roles(new ArrayList<>(List.of(userRole, adminRole))).build();

		// ===== ACT =====
		CustomUserDetails userDetails = new CustomUserDetails(user);

		// ===== ASSERT =====
		assertEquals("testuser", userDetails.getUsername());
		assertEquals("password", userDetails.getPassword());
		assertEquals(2, userDetails.getAuthorities().size());
		assertEquals(2, userDetails.getRole().size());

		// Verify authorities are prefixed with ROLE_
		Set<String> authorities = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(java.util.stream.Collectors.toSet());
		assertTrue(authorities.contains("ROLE_USER"));
		assertTrue(authorities.contains("ROLE_ADMIN"));
	}

	//
	@Test
	@DisplayName("Constructor should handle user without roles")
	void constructor_shouldHandleUserWithoutRoles() {
		// ===== ARRANGE =====
		User user = User.builder().id(1).username("testuser").password("password").fullname("Test User")
				.email("test@example.com").roles(new ArrayList<>()).build();

		// ===== ACT =====
		CustomUserDetails userDetails = new CustomUserDetails(user);

		// ===== ASSERT =====
		assertEquals("testuser", userDetails.getUsername());
		assertEquals("password", userDetails.getPassword());
		assertTrue(userDetails.getAuthorities().isEmpty());
		assertTrue(userDetails.getRole().isEmpty());
	}

	//
	@Test
	@DisplayName("Constructor should handle null roles")
	void constructor_shouldHandleNullRoles() {
		// ===== ARRANGE =====
		User user = User.builder().id(1).username("testuser").password("password").fullname("Test User")
				.email("test@example.com").roles(null).build();

		// ===== ACT =====
		CustomUserDetails userDetails = new CustomUserDetails(user);

		// ===== ASSERT =====
		assertEquals("testuser", userDetails.getUsername());
		assertEquals("password", userDetails.getPassword());
		assertTrue(userDetails.getAuthorities().isEmpty());
		assertTrue(userDetails.getRole().isEmpty());
	}

	@Test
	@DisplayName("Account status methods should return true")
	void accountStatusMethods_shouldReturnTrue() {
		// ===== ARRANGE =====
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User user = User.builder().id(1).username("testuser").password("password")
				.roles(new ArrayList<>(List.of(userRole))).build();

		// ===== ACT =====
		CustomUserDetails userDetails = new CustomUserDetails(user);

		// ===== ASSERT =====
		assertTrue(userDetails.isAccountNonExpired());
		assertTrue(userDetails.isAccountNonLocked());
		assertTrue(userDetails.isCredentialsNonExpired());
		assertTrue(userDetails.isEnabled());
	}

	@Test
	@DisplayName("getRole should return roles")
	void getRole_shouldReturnRoles() {
		// ===== ARRANGE =====
		Role userRole = Role.builder().id(1).roleName("USER").build();
		Role adminRole = Role.builder().id(2).roleName("ADMIN").build();
		User user = User.builder().id(1).username("testuser").password("password")
				.roles(new ArrayList<>(List.of(userRole, adminRole))).build();

		// ===== ACT =====
		CustomUserDetails userDetails = new CustomUserDetails(user);
		Set<Role> roles = userDetails.getRole();

		// ===== ASSERT =====
		assertEquals(2, roles.size());
		assertTrue(roles.stream().anyMatch(r -> r.getRoleName().equals("USER")));
		assertTrue(roles.stream().anyMatch(r -> r.getRoleName().equals("ADMIN")));
	}
}
