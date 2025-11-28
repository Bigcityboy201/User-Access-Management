package com.r2s.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.auth.kafka.UserKafkaProducer;
import com.r2s.core.dto.CreateUserProfileDTO;
import com.r2s.core.dto.request.SignInRequest;
import com.r2s.core.dto.request.SignUpRequest;
import com.r2s.core.dto.request.SignUpRequest.RoleRequest;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.RoleRepository;
import com.r2s.core.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Slf4j
class AuthControllerIntegrationTest {

	private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;
	private static final boolean POSTGRES_AVAILABLE;

	static {
		PostgreSQLContainer<?> container = null;
		boolean started = false;
		try {
			container = new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("auth_service_it")
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
	private ObjectMapper objectMapper;

	@MockBean
	private UserKafkaProducer userKafkaProducer;

	private Role userRole;
	private Role adminRole;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		roleRepository.deleteAll();

		userRole = roleRepository
				.save(Role.builder().roleName("USER").description("Standard user").isActive(true).build());

		adminRole = roleRepository
				.save(Role.builder().roleName("ADMIN").description("Administrator").isActive(true).build());

		reset(userKafkaProducer);
	}

	// ======== REGISTER TESTS ========

	@Test
	@DisplayName("POST /auth/register - Should persist user and emit Kafka event")
	void register_shouldPersistUserAndEmitKafkaEvent() throws Exception {
		// ===== ARRANGE =====
		SignUpRequest request = SignUpRequest.builder().username("newuser").password("Secret#123")
				.email("newuser@example.com").fullName("New User").role(RoleRequest.builder().roleName("ADMIN").build())
				.build();

		// ===== ACT =====
		var result = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// ===== ASSERT =====
		result.andExpect(status().isOk()).andExpect(jsonPath("$.data").value("User registered successfully"))
				.andExpect(jsonPath("$.code").value("OK"));

		User persisted = userRepository.findByUsername("newuser").orElseThrow();
		assertThat(passwordEncoder.matches("Secret#123", persisted.getPassword())).isTrue();
		assertThat(persisted.getRoles()).extracting(Role::getRoleName).containsExactly("ADMIN");

		verify(userKafkaProducer).sendUserRegistered(any(CreateUserProfileDTO.class));
	}

	@Test
	@DisplayName("POST /auth/register - Should fail for duplicate username")
	void register_shouldFailForDuplicateUsername() throws Exception {
		// ===== ARRANGE =====
		userRepository.save(User.builder().username("duplicate").password(passwordEncoder.encode("Secret#123"))
				.email("dup@example.com").fullname("Dup User").roles(List.of(userRole)).deleted(false).build());

		SignUpRequest request = SignUpRequest.builder().username("duplicate").password("AnotherPass1!")
				.email("dup2@example.com").fullName("Dup User 2").build();

		// ===== ACT =====
		var result = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// ===== ASSERT =====
		result.andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONFLICT"))
				.andExpect(jsonPath("$.message", containsString("User already exist")));

		verifyNoInteractions(userKafkaProducer);
	}

	@Test
	@DisplayName("POST /auth/register - Should return 400 when email is invalid")
	void register_shouldReturnBadRequestWhenInvalidEmail() throws Exception {
		// ===== ARRANGE =====
		SignUpRequest request = SignUpRequest.builder().username("invalidemail").password("Secret#123")
				.email("not-an-email").fullName("Invalid Email User").build();

		// ===== ACT =====
		var result = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// ===== ASSERT =====
		result.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message", containsString("email")));
	}

	@Test
	@DisplayName("POST /auth/register - Should return 400 when password is too short")
	void register_shouldReturnBadRequestWhenPasswordTooShort() throws Exception {
		// ===== ARRANGE =====
		SignUpRequest request = SignUpRequest.builder().username("shortpass").password("123").email("short@example.com")
				.fullName("Short Pass").build();

		// ===== ACT =====
		var result = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// ===== ASSERT =====
		result.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message", containsString("password")));
	}

	// ======== LOGIN TESTS ========

	@Test
	@DisplayName("POST /auth/login - Should return JWT for valid credentials")
	void login_shouldReturnJwtForValidCredentials() throws Exception {
		// ===== ARRANGE =====
		userRepository.save(User.builder().username("loginuser").password(passwordEncoder.encode("Secret#123"))
				.email("login@example.com").fullname("Login User").deleted(false).roles(List.of(userRole)).build());

		SignInRequest request = SignInRequest.builder().username("loginuser").password("Secret#123").build();

		// ===== ACT =====
		var result = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// ===== ASSERT =====
		result.andExpect(status().isOk()).andExpect(jsonPath("$.data.token", notNullValue()))
				.andExpect(jsonPath("$.data.expiredDate", notNullValue())).andExpect(jsonPath("$.code").value("OK"));
	}

	@Test
	@DisplayName("POST /auth/login - Should return 401 when password is invalid")
	void login_shouldReturnUnauthorizedWhenPasswordInvalid() throws Exception {
		// ===== ARRANGE =====
		userRepository.save(User.builder().username("loginuser").password(passwordEncoder.encode("Secret#123"))
				.email("login@example.com").fullname("Login User").deleted(false).roles(List.of(userRole)).build());

		SignInRequest request = SignInRequest.builder().username("loginuser").password("WrongPassword!").build();

		// ===== ACT =====
		var result = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// ===== ASSERT =====
		result.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
				.andExpect(jsonPath("$.message").value("Invalid username or password"));
	}

	@Test
	@DisplayName("POST /auth/login - Should return 401 when user not found")
	void login_shouldReturnUnauthorizedWhenUserNotFound() throws Exception {
		// ===== ARRANGE =====
		SignInRequest request = SignInRequest.builder().username("unknownUser").password("SomePassword123!").build();

		// ===== ACT =====
		var result = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// ===== ASSERT =====
		result.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
				.andExpect(jsonPath("$.message").value("Invalid username or password"));
	}
}
