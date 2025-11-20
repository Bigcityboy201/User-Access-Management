package com.r2s.core.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	void builder_shouldCreateUserWithAllFields() {
		// Setup
		Role userRole = Role.builder().id(1).roleName("USER").build();
		List<Role> roles = new ArrayList<>(List.of(userRole));

		// Execute
		User user = User.builder().id(1).username("testuser").password("password")
				.fullname("Test User").email("test@example.com").deleted(false)
				.roles(roles).build();

		// Verify
		assertNotNull(user);
		assertEquals(1, user.getId());
		assertEquals("testuser", user.getUsername());
		assertEquals("password", user.getPassword());
		assertEquals("Test User", user.getFullname());
		assertEquals("test@example.com", user.getEmail());
		assertFalse(user.isDeleted());
		assertEquals(1, user.getRoles().size());
		assertEquals("USER", user.getRoles().get(0).getRoleName());
	}

	@Test
	void builder_shouldCreateUserWithDefaultRoles() {
		// Execute
		User user = User.builder().id(1).username("testuser").password("password")
				.roles(new ArrayList<>()).build();

		// Verify
		assertNotNull(user);
		assertNotNull(user.getRoles());
		assertTrue(user.getRoles().isEmpty());
	}

	@Test
	void builder_shouldCreateUserWithMultipleRoles() {
		// Setup
		Role userRole = Role.builder().id(1).roleName("USER").build();
		Role adminRole = Role.builder().id(2).roleName("ADMIN").build();
		List<Role> roles = new ArrayList<>(List.of(userRole, adminRole));

		// Execute
		User user = User.builder().id(1).username("testuser").password("password")
				.roles(roles).build();

		// Verify
		assertNotNull(user);
		assertEquals(2, user.getRoles().size());
	}

	@Test
	void setterAndGetter_shouldWorkCorrectly() {
		// Setup
		User user = new User();

		// Execute
		user.setId(1);
		user.setUsername("testuser");
		user.setPassword("password");
		user.setFullname("Test User");
		user.setEmail("test@example.com");
		user.setDeleted(true);

		// Verify
		assertEquals(1, user.getId());
		assertEquals("testuser", user.getUsername());
		assertEquals("password", user.getPassword());
		assertEquals("Test User", user.getFullname());
		assertEquals("test@example.com", user.getEmail());
		assertTrue(user.isDeleted());
	}
}

