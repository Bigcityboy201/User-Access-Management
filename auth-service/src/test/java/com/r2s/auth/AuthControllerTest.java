package com.r2s.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.auth.service.UserService;
import com.r2s.core.dto.request.SignInRequest;
import com.r2s.core.dto.request.SignUpRequest;
import com.r2s.core.dto.response.SignInResponse;
import com.r2s.core.util.JwtUtils;

@SpringBootTest
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
	void register_shouldReturnSuccessMessage() throws Exception {
		// Setup
		SignUpRequest request = new SignUpRequest();
		request.setUsername("john");
		request.setPassword("123456");
		request.setEmail("john@example.com");
		request.setFullName("John Doe");

		when(userService.signUp(any(SignUpRequest.class))).thenReturn(true);

		// Execute
		ResultActions response = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// Verify
		response.andExpect(status().isOk()).andExpect(jsonPath("$").value("User registered successfully"));

		verify(userService).signUp(any(SignUpRequest.class));
	}

	// === POST /auth/register - username already exists ===
	@Test
	void register_shouldReturnErrorIfUsernameExists() throws Exception {
		// Setup
		SignUpRequest request = new SignUpRequest();
		request.setUsername("john");
		request.setPassword("123456");
		request.setEmail("john@example.com");
		request.setFullName("John Doe");

		when(userService.signUp(any(SignUpRequest.class)))
				.thenThrow(new RuntimeException("User with userName: john already existed!"));

		// Execute
		ResultActions response = mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// Verify
		response.andExpect(status().isInternalServerError());

		verify(userService).signUp(any(SignUpRequest.class));
	}

	// === POST /auth/login ===
	@Test
	void login_shouldReturnSignInResponse() throws Exception {
		// Setup
		SignInRequest request = new SignInRequest();
		request.setUsername("john");
		request.setPassword("123456");

		SignInResponse signInResponse = SignInResponse.builder().token("test-jwt-token")
				.expiredDate(new Date(System.currentTimeMillis() + 86400000)).build();

		when(userService.signIn(any(SignInRequest.class))).thenReturn(signInResponse);

		// Execute
		ResultActions response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// Verify
		response.andExpect(status().isOk()).andExpect(jsonPath("$.token").value("test-jwt-token"))
				.andExpect(jsonPath("$.expiredDate").exists());

		verify(userService).signIn(any(SignInRequest.class));
	}

	// === POST /auth/login - invalid credentials ===
	@Test
	void login_shouldReturnErrorIfInvalidCredentials() throws Exception {
		// Setup
		SignInRequest request = new SignInRequest();
		request.setUsername("john");
		request.setPassword("wrongPassword");

		when(userService.signIn(any(SignInRequest.class)))
				.thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

		// Execute
		ResultActions response = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)));

		// Verify
		response.andExpect(status().isUnauthorized());

		verify(userService).signIn(any(SignInRequest.class));
	}
}
