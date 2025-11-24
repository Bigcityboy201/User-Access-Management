package com.r2s.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.RoleRepository;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.security.CustomUserDetails;
import com.r2s.core.util.JwtUtils;

import lombok.extern.slf4j.Slf4j;

@org.springframework.boot.test.context.SpringBootTest(classes = TestConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test-no-docker")
@Slf4j
@DisplayName("Cross-Service System Integration Tests (No Docker)")
class CrossServiceIntegrationTestNoDocker {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtils jwtUtils;

	@BeforeEach
	void setUp() {
		if (userRepository != null) {
			userRepository.deleteAll();
		}
		if (roleRepository != null) {
			roleRepository.deleteAll();
		}
	}

	@Test
	@DisplayName("Should generate and validate JWT token for cross-service authentication")
	void testJwtTokenGenerationAndValidation() {
		// Setup: Create role and user (simulating auth-service registration)
		Role userRole = roleRepository
				.save(Role.builder().roleName("USER").description("Standard user").isActive(true).build());

		User user = userRepository.save(User.builder().username("systemtest")
				.password(passwordEncoder.encode("Test@123")).email("systemtest@example.com")
				.fullname("System Test User").deleted(false).roles(new ArrayList<>(List.of(userRole))).build());

		// Test: Generate JWT token (auth-service responsibility)
		CustomUserDetails userDetails = new CustomUserDetails(user);
		String token = jwtUtils.generateToken(userDetails);

		// Verify: Token is generated
		assertNotNull(token);
		assertTrue(token.length() > 0);

		// Test: Validate token (core module - used by both services)
		boolean isValid = jwtUtils.validateToken(token);
		assertTrue(isValid);

		// Test: Extract username from token (user-service uses this)
		String extractedUsername = jwtUtils.extractUsername(token);
		assertThat(extractedUsername).isEqualTo("systemtest");

		log.info("JWT token generated and validated successfully for user: {}", extractedUsername);
	}

	@Test
	@DisplayName("Should maintain user data consistency across services")
	void testUserDataConsistency() {
		// Setup: Create role and user
		Role adminRole = roleRepository
				.save(Role.builder().roleName("ADMIN").description("Administrator").isActive(true).build());

		User user = userRepository.save(User.builder().username("consistencytest")
				.password(passwordEncoder.encode("Test@123")).email("consistency@example.com")
				.fullname("Consistency Test").deleted(false).roles(new ArrayList<>(List.of(adminRole))).build());

		// Test: Verify user can be retrieved (both services use same repository)
		User retrievedUser = userRepository.findByUsername("consistencytest")
				.orElseThrow(() -> new RuntimeException("User not found"));

		// Verify: Data consistency
		assertThat(retrievedUser.getUsername()).isEqualTo("consistencytest");
		assertThat(retrievedUser.getEmail()).isEqualTo("consistency@example.com");
		assertThat(retrievedUser.getRoles()).hasSize(1);
		assertThat(retrievedUser.getRoles().get(0).getRoleName()).isEqualTo("ADMIN");

		// Test: Verify password encoding (auth-service encodes, user-service validates)
		assertTrue(passwordEncoder.matches("Test@123", retrievedUser.getPassword()));

		log.info("User data consistency verified across services");
	}

	@Test
	@DisplayName("Should handle role-based authentication for cross-service access")
	void testRoleBasedAuthentication() {
		// Setup: Create multiple roles
		Role userRole = roleRepository
				.save(Role.builder().roleName("USER").description("Standard user").isActive(true).build());

		Role adminRole = roleRepository
				.save(Role.builder().roleName("ADMIN").description("Administrator").isActive(true).build());

		// Create user with multiple roles
		User user = userRepository.save(User.builder().username("multirole")
				.password(passwordEncoder.encode("Test@123")).email("multirole@example.com").fullname("Multi Role User")
				.deleted(false).roles(new ArrayList<>(List.of(userRole, adminRole))).build());

		// Test: Generate token with multiple roles
		CustomUserDetails userDetails = new CustomUserDetails(user);
		String token = jwtUtils.generateToken(userDetails);

		// Verify: Token contains role information
		assertNotNull(token);
		assertTrue(jwtUtils.validateToken(token));

		// Verify: User details contain all roles
		assertThat(userDetails.getAuthorities()).hasSize(2);
		assertThat(userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).toList())
				.containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");

		log.info("Role-based authentication verified for user with multiple roles");
	}
}
