package com.r2s.core.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	@DisplayName("Builder should create user with all fields")
	void builder_shouldCreateUserWithAllFields() {
		// ===== ARRANGE =====
		Role userRole = Role.builder().id(1).roleName("USER").build();
		List<Role> roles = new ArrayList<>(List.of(userRole));

		// ===== ACT =====
		User user = User.builder().id(1).username("testuser").password("password").fullname("Test User")
				.email("test@example.com").deleted(false).roles(roles).build();

		// ===== ASSERT =====
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
	@DisplayName("Builder should create user with default roles")
	void builder_shouldCreateUserWithDefaultRoles() {
		// ===== ACT =====
		User user = User.builder().id(1).username("testuser").password("password").roles(new ArrayList<>()).build();

		// ===== ASSERT =====
		assertNotNull(user);
		assertNotNull(user.getRoles());
		assertTrue(user.getRoles().isEmpty());
	}

	@Test
	@DisplayName("Builder should create user with multiple roles")
	void builder_shouldCreateUserWithMultipleRoles() {
		// ===== ARRANGE =====
		Role userRole = Role.builder().id(1).roleName("USER").build();
		Role adminRole = Role.builder().id(2).roleName("ADMIN").build();
		List<Role> roles = new ArrayList<>(List.of(userRole, adminRole));

		// ===== ACT =====
		User user = User.builder().id(1).username("testuser").password("password").roles(roles).build();

		// ===== ASSERT =====
		assertNotNull(user);
		assertEquals(2, user.getRoles().size());
	}

	@Test
	@DisplayName("Setter and getter should work correctly")
	void setterAndGetter_shouldWorkCorrectly() {
		// ===== ARRANGE =====
		User user = new User();

		// ===== ACT =====
		user.setId(1);
		user.setUsername("testuser");
		user.setPassword("password");
		user.setFullname("Test User");
		user.setEmail("test@example.com");
		user.setDeleted(true);

		// ===== ASSERT =====
		assertEquals(1, user.getId());
		assertEquals("testuser", user.getUsername());
		assertEquals("password", user.getPassword());
		assertEquals("Test User", user.getFullname());
		assertEquals("test@example.com", user.getEmail());
		assertTrue(user.isDeleted());
	}

	//
	@Test
	@DisplayName("Builder should handle null roles")
	void builder_shouldHandleNullRoles() {
		// ===== ACT =====
		User user = User.builder().id(1).username("testuser").password("password").roles(null).build();

		// ===== ASSERT =====
		assertNotNull(user);
		assertNotNull(user.getRoles());
		assertTrue(user.getRoles().isEmpty());
	}

	//
	@Test
	@DisplayName("Equals and hashCode should work correctly")
	void equalsAndHashCode_shouldWorkCorrectly() {
		// ===== ARRANGE =====
		User user1 = User.builder().id(1).username("testuser").build();
		User user2 = User.builder().id(1).username("testuser").build();
		User user3 = User.builder().id(2).username("otheruser").build();

		// ===== ASSERT =====
		assertEquals(user1, user2);
		assertEquals(user1.hashCode(), user2.hashCode());
		assertNotEquals(user1, user3);
	}

	//
	@Test
	@DisplayName("toString should contain username")
	void toString_shouldContainUsername() {
		// ===== ARRANGE =====
		User user = User.builder().id(1).username("testuser").build();

		// ===== ACT =====
		String str = user.toString();

		// ===== ASSERT =====
		assertTrue(str.contains("testuser"));
	}
}
