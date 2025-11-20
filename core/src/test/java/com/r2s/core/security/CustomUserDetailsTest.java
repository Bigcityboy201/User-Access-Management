package com.r2s.core.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;

class CustomUserDetailsTest {

	@Test
	void constructor_shouldCreateUserDetailsWithRoles() {
		// Setup
		Role userRole = Role.builder().id(1).roleName("USER").build();
		Role adminRole = Role.builder().id(2).roleName("ADMIN").build();
		User user = User.builder().id(1).username("testuser").password("password")
				.fullname("Test User").email("test@example.com")
				.roles(new ArrayList<>(List.of(userRole, adminRole))).build();

		// Execute
		CustomUserDetails userDetails = new CustomUserDetails(user);

		// Verify
		assertEquals("testuser", userDetails.getUsername());
		assertEquals("password", userDetails.getPassword());
		assertEquals(2, userDetails.getAuthorities().size());
		assertEquals(2, userDetails.getRole().size());

		// Verify authorities are prefixed with ROLE_
		Set<String> authorities = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority).collect(java.util.stream.Collectors.toSet());
		assertTrue(authorities.contains("ROLE_USER"));
		assertTrue(authorities.contains("ROLE_ADMIN"));
	}

	@Test
	void constructor_shouldHandleUserWithoutRoles() {
		// Setup
		User user = User.builder().id(1).username("testuser").password("password")
				.fullname("Test User").email("test@example.com")
				.roles(new ArrayList<>()).build();

		// Execute
		CustomUserDetails userDetails = new CustomUserDetails(user);

		// Verify
		assertEquals("testuser", userDetails.getUsername());
		assertEquals("password", userDetails.getPassword());
		assertTrue(userDetails.getAuthorities().isEmpty());
		assertTrue(userDetails.getRole().isEmpty());
	}

	@Test
	void constructor_shouldHandleNullRoles() {
		// Setup
		User user = User.builder().id(1).username("testuser").password("password")
				.fullname("Test User").email("test@example.com")
				.roles(null).build();

		// Execute
		CustomUserDetails userDetails = new CustomUserDetails(user);

		// Verify
		assertEquals("testuser", userDetails.getUsername());
		assertTrue(userDetails.getAuthorities().isEmpty());
		assertTrue(userDetails.getRole().isEmpty());
	}

	@Test
	void accountStatusMethods_shouldReturnTrue() {
		// Setup
		Role userRole = Role.builder().id(1).roleName("USER").build();
		User user = User.builder().id(1).username("testuser").password("password")
				.roles(new ArrayList<>(List.of(userRole))).build();
		CustomUserDetails userDetails = new CustomUserDetails(user);

		// Verify
		assertTrue(userDetails.isAccountNonExpired());
		assertTrue(userDetails.isAccountNonLocked());
		assertTrue(userDetails.isCredentialsNonExpired());
		assertTrue(userDetails.isEnabled());
	}

	@Test
	void getRole_shouldReturnRoles() {
		// Setup
		Role userRole = Role.builder().id(1).roleName("USER").build();
		Role adminRole = Role.builder().id(2).roleName("ADMIN").build();
		User user = User.builder().id(1).username("testuser").password("password")
				.roles(new ArrayList<>(List.of(userRole, adminRole))).build();
		CustomUserDetails userDetails = new CustomUserDetails(user);

		// Execute
		Set<Role> roles = userDetails.getRole();

		// Verify
		assertEquals(2, roles.size());
		assertTrue(roles.stream().anyMatch(r -> r.getRoleName().equals("USER")));
		assertTrue(roles.stream().anyMatch(r -> r.getRoleName().equals("ADMIN")));
	}
}

