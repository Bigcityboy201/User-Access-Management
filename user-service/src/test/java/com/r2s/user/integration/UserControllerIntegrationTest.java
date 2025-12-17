package com.r2s.user.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.RoleRepository;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.security.CustomUserDetails;
import com.r2s.core.util.JwtUtils;
import com.r2s.user.dto.UserResponse.UpdateUserRequest;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Slf4j
class UserControllerIntegrationTest {

	private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;
	private static final boolean POSTGRES_AVAILABLE;

	static {
		PostgreSQLContainer<?> container = null;
		boolean started = false;
		try {
			container = new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("user_service_it")
					.withUsername("it_user").withPassword("it_password");
			container.start();
			started = true;
			log.info("Started PostgreSQL testcontainer at {}", container.getJdbcUrl());
		} catch (Throwable throwable) {
			log.warn("Unable to start PostgreSQL testcontainer. Falling back to external datasource. Reason: {}",
					throwable.getMessage());
		}
		POSTGRES_CONTAINER = container;
		POSTGRES_AVAILABLE = started;
	}

	@DynamicPropertySource
	static void overrideDatasourceProps(DynamicPropertyRegistry registry) {
		if (POSTGRES_AVAILABLE) {
			registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
			registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
			registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
			registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
		}
	}

	@AfterAll
	static void shutDownContainer() {
		if (POSTGRES_AVAILABLE && POSTGRES_CONTAINER != null) {
			POSTGRES_CONTAINER.stop();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private ObjectMapper objectMapper;

	private User adminUser;
	private User normalUser;
	private Role adminRole;
	private Role userRole;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		roleRepository.deleteAll();

		adminRole = roleRepository
				.save(Role.builder().roleName("ADMIN").description("Administrator").isActive(true).build());

		userRole = roleRepository
				.save(Role.builder().roleName("USER").description("Standard user").isActive(true).build());

		adminUser = userRepository.save(
				User.builder().username("admin").password(passwordEncoder.encode("Admin#123")).fullname("Admin User")
						.email("admin@example.com").deleted(false).roles(new ArrayList<>(List.of(adminRole))).build());

		normalUser = userRepository
				.save(User.builder().username("john").password(passwordEncoder.encode("User#123")).fullname("John Doe")
						.email("john@example.com").deleted(false).roles(new ArrayList<>(List.of(userRole))).build());
	}

	// ===== AAA pattern: Arrange - Act - Assert =====

	@Test
	@DisplayName("getAllUsers - Should return data when admin has valid token")
	void getAllUsers_shouldReturnData_whenAdminHasValidToken() throws Exception {
		// ===== ACT & ASSERT =====
		mockMvc.perform(get("/users").header("Authorization", bearerTokenFor(adminUser))).andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("OK")).andExpect(jsonPath("$.data", hasSize(2)))
				.andExpect(jsonPath("$.data[0].username", notNullValue()));
	}

	//
	@Test
	@DisplayName("getAllUsers - Should return forbidden when user role")
	void getAllUsers_shouldReturnForbidden_whenUserRole() throws Exception {
		// ===== ACT & ASSERT =====
		mockMvc.perform(get("/users").header("Authorization", bearerTokenFor(normalUser)))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("getMyProfile - Should return current user details")
	void getMyProfile_shouldReturnCurrentUserDetails_whenValidToken() throws Exception {
		// ===== ACT & ASSERT =====
		mockMvc.perform(get("/users/me").header("Authorization", bearerTokenFor(normalUser))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.username").value("john"))
				.andExpect(jsonPath("$.data.email").value("john@example.com"))
				.andExpect(jsonPath("$.code").value("OK"));
	}

	@Test
	@DisplayName("updateMyProfile - Should persist and return updated payload")
	void updateMyProfile_shouldPersistAndReturnUpdatedPayload_whenValidToken() throws Exception {
		// ===== ARRANGE =====
		UpdateUserRequest request = new UpdateUserRequest("updated@example.com", "John Updated");

		// ===== ACT & ASSERT =====
		mockMvc.perform(put("/users/me").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", bearerTokenFor(normalUser)).content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data.fullname").value("John Updated"))
				.andExpect(jsonPath("$.data.email").value("updated@example.com"))
				.andExpect(jsonPath("$.code").value("OK"));

		// ===== ASSERT DB =====
		User updated = userRepository.findByUsername("john").orElseThrow();
		org.assertj.core.api.Assertions.assertThat(updated.getFullname()).isEqualTo("John Updated");
		org.assertj.core.api.Assertions.assertThat(updated.getEmail()).isEqualTo("updated@example.com");
	}

	//
	@Test
	@DisplayName("updateMyProfile - Should return unauthorized when invalid token")
	void updateMyProfile_shouldReturnUnauthorized_whenInvalidToken() throws Exception {
		// ===== ARRANGE =====
		UpdateUserRequest request = new UpdateUserRequest("invalid@example.com", "Invalid User");

		// ===== ACT & ASSERT =====
		mockMvc.perform(put("/users/me").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer InvalidToken").content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("deleteUser - Should remove user from database")
	void deleteUser_shouldRemoveUserFromDatabase_whenAdminToken() throws Exception {
		// ===== ARRANGE =====
		User temp = userRepository
				.save(User.builder().username("temp").password(passwordEncoder.encode("Temp#123")).fullname("Temp User")
						.email("temp@example.com").deleted(false).roles(new ArrayList<>(List.of(userRole))).build());

		// ===== ACT & ASSERT =====
		mockMvc.perform(delete("/users/{username}", "temp").header("Authorization", bearerTokenFor(adminUser)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.code").value("OK"))
				.andExpect(jsonPath("$.data", is("Xóa Thành Công with UserNametemp")));

		// ===== ASSERT DB =====
		org.assertj.core.api.Assertions.assertThat(userRepository.findByUsername("temp")).isEmpty();
	}

	//
	@Test
	@DisplayName("deleteUser - Should return forbidden when non-admin token")
	void deleteUser_shouldReturnForbidden_whenNonAdmin() throws Exception {
		// ===== ACT & ASSERT =====
		mockMvc.perform(delete("/users/{username}", "john").header("Authorization", bearerTokenFor(normalUser)))
				.andExpect(status().isForbidden());
	}

	private String bearerTokenFor(User user) {
		return "Bearer " + jwtUtils.generateToken(new CustomUserDetails(user));
	}
}
