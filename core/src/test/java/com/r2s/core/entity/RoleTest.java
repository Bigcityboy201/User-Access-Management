package com.r2s.core.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

	@Test
	@DisplayName("Builder should create role with all fields")
	void builder_shouldCreateRoleWithAllFields() {
		// ===== ACT =====
		Role role = Role.builder().id(1).roleName("ADMIN").description("Administrator role").isActive(true).build();

		// ===== ASSERT =====
		assertNotNull(role);
		assertEquals(1, role.getId());
		assertEquals("ADMIN", role.getRoleName());
		assertEquals("Administrator role", role.getDescription());
		assertTrue(role.isActive());
	}

	@Test
	@DisplayName("Builder should create role with minimal fields")
	void builder_shouldCreateRoleWithMinimalFields() {
		// ===== ACT =====
		Role role = Role.builder().id(1).roleName("USER").build();

		// ===== ASSERT =====
		assertNotNull(role);
		assertEquals(1, role.getId());
		assertEquals("USER", role.getRoleName());
	}

	@Test
	@DisplayName("Setter and getter should work correctly")
	void setterAndGetter_shouldWorkCorrectly() {
		// ===== ARRANGE =====
		Role role = new Role();

		// ===== ACT =====
		role.setId(1);
		role.setRoleName("MODERATOR");
		role.setDescription("Moderator role");
		role.setActive(false);

		// ===== ASSERT =====
		assertEquals(1, role.getId());
		assertEquals("MODERATOR", role.getRoleName());
		assertEquals("Moderator role", role.getDescription());
		assertFalse(role.isActive());
	}

	@Test
	@DisplayName("No-args constructor should create empty role")
	void noArgsConstructor_shouldCreateEmptyRole() {
		// ===== ACT =====
		Role role = new Role();

		// ===== ASSERT =====
		assertNotNull(role);
	}

	// ===== New Test Cases =====

	//
	@Test
	@DisplayName("Builder should handle null description")
	void builder_shouldHandleNullDescription() {
		// ===== ACT =====
		Role role = Role.builder().id(1).roleName("USER").description(null).build();

		// ===== ASSERT =====
		assertNotNull(role);
		assertEquals(1, role.getId());
		assertEquals("USER", role.getRoleName());
		assertNull(role.getDescription());
	}

	//
	@Test
	@DisplayName("Equals and hashCode should work correctly")
	void equalsAndHashCode_shouldWorkCorrectly() {
		// ===== ARRANGE =====
		Role role1 = Role.builder().id(1).roleName("USER").build();
		Role role2 = Role.builder().id(1).roleName("USER").build();
		Role role3 = Role.builder().id(2).roleName("ADMIN").build();

		// ===== ASSERT =====
		assertEquals(role1, role2);
		assertEquals(role1.hashCode(), role2.hashCode());
		assertNotEquals(role1, role3);
	}

	//
	@Test
	@DisplayName("toString should contain roleName")
	void toString_shouldContainRoleName() {
		// ===== ARRANGE =====
		Role role = Role.builder().id(1).roleName("USER").build();

		// ===== ACT =====
		String str = role.toString();

		// ===== ASSERT =====
		assertTrue(str.contains("USER"));
	}
}
