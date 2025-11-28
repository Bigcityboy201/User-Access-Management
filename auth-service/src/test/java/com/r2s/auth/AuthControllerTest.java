package com.r2s.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.auth.service.UserService;
import com.r2s.core.dto.request.SignInRequest;
import com.r2s.core.dto.request.SignUpRequest;
import com.r2s.core.dto.response.SignInResponse;
import com.r2s.core.util.JwtUtils;

//1.thêm displayName cho từng method
//2.sửa tên method theo dạng:methodName_shouldReturnExpected_whenCondition
//3.sửa cơ chế AAA pattern
@SpringBootTest
@ComponentScan(basePackages = { "com.r2s.auth", "com.r2s.core.handler" })
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private AuthenticationManager authenticationManager;

	@MockBean
	private JwtUtils jwtUtils;

	private final ObjectMapper objectMapper = new ObjectMapper();

	// === POST /auth/register ===
	@Test
	@DisplayName("POST /auth/register - Should return success when user registers successfully")
	void register_shouldReturnSuccess_whenUserRegisters() throws Exception {
		// Arrange
		SignUpRequest request = new SignUpRequest();
		request.setUsername("john");
		request.setPassword("123456");
		request.setEmail("john@example.com");
		request.setFullName("John Doe");

		when(userService.signUp(any(SignUpRequest.class))).thenReturn(true);

		// Act
		ResultActions response = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// Assert
		response.andExpect(status().isOk()).andExpect(jsonPath("$.data").value("User registered successfully"));

		verify(userService).signUp(any(SignUpRequest.class));
	}

	// === POST /auth/register - username already exists ===
	@Test
	@DisplayName("POST /auth/register - Should return error when username already exists")
	void register_shouldReturnError_whenUsernameExists() throws Exception {
		// Arrange
		SignUpRequest request = new SignUpRequest();
		request.setUsername("john");
		request.setPassword("123456");
		request.setEmail("john@example.com");
		request.setFullName("John Doe");

		when(userService.signUp(any(SignUpRequest.class)))
				.thenThrow(new RuntimeException("User with userName: john already existed!"));

		// Act
		ResultActions response = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// Assert
		response.andExpect(status().isInternalServerError());

		verify(userService).signUp(any(SignUpRequest.class));
	}

	// === POST /auth/login ===
	@Test
	@DisplayName("POST /auth/login - Should return token when login succeeds")
	void login_shouldReturnToken_whenCredentialsValid() throws Exception {
		// Arrange
		SignInRequest request = new SignInRequest();
		request.setUsername("john");
		request.setPassword("123456");

		SignInResponse signInResponse = SignInResponse.builder().token("test-jwt-token")
				.expiredDate(new Date(System.currentTimeMillis() + 86400000)).build();

		when(userService.signIn(any(SignInRequest.class))).thenReturn(signInResponse);

		// Act
		ResultActions response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// Assert
		response.andExpect(status().isOk()).andExpect(jsonPath("$.data.token").value("test-jwt-token"))
				.andExpect(jsonPath("$.data.expiredDate").exists());

		verify(userService).signIn(any(SignInRequest.class));
	}

	@Test
	@DisplayName("POST /auth/login - Should return 401 when credentials are invalid")
	void login_shouldReturnUnauthorized_whenCredentialsInvalid() throws Exception {
		// Arrange
		SignInRequest request = new SignInRequest();
		request.setUsername("john");
		request.setPassword("wrongPassword");

		when(userService.signIn(any(SignInRequest.class))).thenThrow(new BadCredentialsException("Bad credentials"));

		// Act
		ResultActions response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// Assert
		response.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
				.andExpect(jsonPath("$.message").value("Invalid username or password"))
				.andExpect(jsonPath("$.domain").value("auth"));
		verify(userService).signIn(any(SignInRequest.class));
	}
}
