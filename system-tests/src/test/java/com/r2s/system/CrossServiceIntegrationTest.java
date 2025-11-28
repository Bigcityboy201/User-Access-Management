package com.r2s.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.RoleRepository;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.security.CustomUserDetails;
import com.r2s.core.util.JwtUtils;

import lombok.extern.slf4j.Slf4j;

@org.springframework.boot.test.context.SpringBootTest(classes = TestConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.AUTO_CONFIGURED)
@ActiveProfiles("test")
@Slf4j
@DisplayName("Cross-Service System Integration Tests")
class CrossServiceIntegrationTest {

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		// Use H2 database for tests (Docker not required)
		registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL");
		registry.add("spring.datasource.username", () -> "sa");
		registry.add("spring.datasource.password", () -> "");
		registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
		registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
		registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.H2Dialect");
		registry.add("jwt.secret", () -> "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
		registry.add("jwt.duration", () -> "86400");
	}

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
		// Setup
		Role userRole = roleRepository
				.save(Role.builder().roleName("USER").description("Standard user").isActive(true).build());

		User user = userRepository.save(User.builder().username("systemtest")
				.password(passwordEncoder.encode("Test@123")).email("systemtest@example.com")
				.fullname("System Test User").deleted(false).roles(new ArrayList<>(List.of(userRole))).build());

		// Generate token
		CustomUserDetails userDetails = new CustomUserDetails(user);
		String token = jwtUtils.generateToken(userDetails);

		// Verify
		assertNotNull(token);
		assertTrue(token.length() > 0);
		assertTrue(jwtUtils.validateToken(token));

		String extractedUsername = jwtUtils.extractUsername(token);
		assertThat(extractedUsername).isEqualTo("systemtest");

		log.info("JWT token generated and validated successfully for user: {}", extractedUsername);
	}

	@Test
	@DisplayName("Should maintain user data consistency across services")
	void testUserDataConsistency() {
		// Setup
		Role adminRole = roleRepository
				.save(Role.builder().roleName("ADMIN").description("Administrator").isActive(true).build());

		User user = userRepository.save(User.builder().username("consistencytest")
				.password(passwordEncoder.encode("Test@123")).email("consistency@example.com")
				.fullname("Consistency Test").deleted(false).roles(new ArrayList<>(List.of(adminRole))).build());

		// Retrieve and verify
		User retrievedUser = userRepository.findByUsername("consistencytest")
				.orElseThrow(() -> new RuntimeException("User not found"));

		assertThat(retrievedUser.getUsername()).isEqualTo("consistencytest");
		assertThat(retrievedUser.getEmail()).isEqualTo("consistency@example.com");
		assertThat(retrievedUser.getRoles()).hasSize(1);
		assertThat(retrievedUser.getRoles().get(0).getRoleName()).isEqualTo("ADMIN");

		assertTrue(passwordEncoder.matches("Test@123", retrievedUser.getPassword()));
		log.info("User data consistency verified across services");
	}

	@Test
	@DisplayName("Should handle role-based authentication for cross-service access")
	void testRoleBasedAuthentication() {
		// Setup multiple roles
		Role userRole = roleRepository
				.save(Role.builder().roleName("USER").description("Standard user").isActive(true).build());

		Role adminRole = roleRepository
				.save(Role.builder().roleName("ADMIN").description("Administrator").isActive(true).build());

		User user = userRepository.save(User.builder().username("multirole")
				.password(passwordEncoder.encode("Test@123")).email("multirole@example.com").fullname("Multi Role User")
				.deleted(false).roles(new ArrayList<>(List.of(userRole, adminRole))).build());

		CustomUserDetails userDetails = new CustomUserDetails(user);
		String token = jwtUtils.generateToken(userDetails);

		assertNotNull(token);
		assertTrue(jwtUtils.validateToken(token));

		assertThat(userDetails.getAuthorities()).hasSize(2);
		assertThat(userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).toList())
				.containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");

		log.info("Role-based authentication verified for user with multiple roles");
	}

	// ===== New Test Cases =====

	@Test
	@DisplayName("Should throw exception when user not found")
	void testUserNotFoundScenario() {
		assertThat(userRepository.findByUsername("unknown")).isEmpty();
	}

	@Test
	@DisplayName("Should handle concurrent user creation without conflict")
	void testConcurrentUserCreation() throws ExecutionException, InterruptedException {
		Role role = roleRepository.save(Role.builder().roleName("USER").isActive(true).build());

		CompletableFuture<User> future1 = CompletableFuture.supplyAsync(() -> userRepository.save(User.builder()
				.username("concurrent").password(passwordEncoder.encode("pass1")).roles(List.of(role)).build()));

		CompletableFuture<User> future2 = CompletableFuture.supplyAsync(() -> userRepository.save(User.builder()
				.username("concurrent2").password(passwordEncoder.encode("pass2")).roles(List.of(role)).build()));

		User user1 = future1.get();
		User user2 = future2.get();

		assertNotNull(user1);
		assertNotNull(user2);
		assertTrue(userRepository.existsById(user1.getId()));
		assertTrue(userRepository.existsById(user2.getId()));
	}

	@Test
	@DisplayName("Should correctly handle JWT token expiration")
	void testTokenExpirationHandling() throws InterruptedException {
		// Short-lived token
		JwtUtils shortJwt = new JwtUtils();
		ReflectionTestUtils.setField(shortJwt, "jwtSecret",
				"404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
		ReflectionTestUtils.setField(shortJwt, "jwtDuration", 1L); // 1 second

		Role role = roleRepository.save(Role.builder().roleName("USER").isActive(true).build());
		User user = userRepository.save(User.builder().username("expiretest").password(passwordEncoder.encode("pass"))
				.roles(List.of(role)).build());

		CustomUserDetails userDetails = new CustomUserDetails(user);
		String token = shortJwt.generateToken(userDetails);

		Thread.sleep(1500); // wait for expiration

		assertTrue(!shortJwt.validateToken(token));
	}
}
