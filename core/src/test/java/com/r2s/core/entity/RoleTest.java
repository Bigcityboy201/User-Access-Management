package com.r2s.core.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RoleTest {

	@Test
	void builder_shouldCreateRoleWithAllFields() {
		// Execute
		Role role = Role.builder().id(1).roleName("ADMIN")
				.description("Administrator role").isActive(true).build();

		// Verify
		assertNotNull(role);
		assertEquals(1, role.getId());
		assertEquals("ADMIN", role.getRoleName());
		assertEquals("Administrator role", role.getDescription());
		assertTrue(role.isActive());
	}

	@Test
	void builder_shouldCreateRoleWithMinimalFields() {
		// Execute
		Role role = Role.builder().id(1).roleName("USER").build();

		// Verify
		assertNotNull(role);
		assertEquals(1, role.getId());
		assertEquals("USER", role.getRoleName());
	}

	@Test
	void setterAndGetter_shouldWorkCorrectly() {
		// Setup
		Role role = new Role();

		// Execute
		role.setId(1);
		role.setRoleName("MODERATOR");
		role.setDescription("Moderator role");
		role.setActive(false);

		// Verify
		assertEquals(1, role.getId());
		assertEquals("MODERATOR", role.getRoleName());
		assertEquals("Moderator role", role.getDescription());
		assertFalse(role.isActive());
	}

	@Test
	void noArgsConstructor_shouldCreateEmptyRole() {
		// Execute
		Role role = new Role();

		// Verify
		assertNotNull(role);
	}
}

