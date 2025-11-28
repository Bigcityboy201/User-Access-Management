package com.r2s.user;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.user.dto.UserResponse;
import com.r2s.user.dto.UserResponse.UpdateUserRequest;
import com.r2s.user.service.UserService;
import com.r2s.user.service.IMPL.UserServiceIMPL;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackages = "com.r2s.user", excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = UserServiceIMPL.class))
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	// === GET /users - admin role ===
	@Test
	@DisplayName("GET /users - Should return list of users for ADMIN")
	@WithMockUser(username = "admin", roles = { "ADMIN" })
	void getAllUsers_shouldReturnListOfUsers_whenAdmin() throws Exception {
		// ===== ARRANGE =====
		Role adminRole = Role.builder().id(1).roleName("ADMIN").build();
		Role userRole = Role.builder().id(2).roleName("USER").build();

		List<User> mockUsers = List.of(
				User.builder().id(1).username("admin").fullname("Admin").email("admin@example.com")
						.roles(List.of(adminRole)).build(),
				User.builder().id(2).username("jane").fullname("Jane Smith").email("jane@example.com")
						.roles(List.of(userRole)).build());

		when(userService.getAllUsers()).thenReturn(mockUsers);

		// ===== ACT ===== & ===== ASSERT =====
		mockMvc.perform(get("/users")).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(2))).andExpect(jsonPath("$.data[0].username").value("admin"))
				.andExpect(jsonPath("$.data[1].username").value("jane")).andExpect(jsonPath("$.code").value("OK"));

		verify(userService).getAllUsers();
	}

	// === GET /users/me ===
	@Test
	@DisplayName("GET /users/me - Should return user profile for authenticated user")
	@WithMockUser(username = "john", roles = { "USER" })
	void getMyProfile_shouldReturnUserProfile_whenAuthenticated() throws Exception {
		// ===== ARRANGE =====
		UserResponse mockResponse = new UserResponse();
		mockResponse.setUsername("john");
		mockResponse.setFullname("John Doe");
		mockResponse.setEmail("john@example.com");
		mockResponse.setRole(List.of("USER"));
		mockResponse.setId(1);

		when(userService.getUserByUsername("john")).thenReturn(mockResponse);

		// ===== ACT & ASSERT =====
		mockMvc.perform(get("/users/me")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.username").value("john"))
				.andExpect(jsonPath("$.data.fullname").value("John Doe"))
				.andExpect(jsonPath("$.data.email").value("john@example.com"))
				.andExpect(jsonPath("$.code").value("OK"));

		verify(userService).getUserByUsername("john");
	}

	@Test
	@DisplayName("GET /users/me - Should return 401 Unauthorized when no auth")
	void getMyProfile_shouldReturnUnauthorized_whenNoAuth() throws Exception {
		// ===== ACT & ASSERT =====
		mockMvc.perform(get("/users/me")).andExpect(status().isUnauthorized());
	}

	// === PUT /users/me ===
	@Test
	@DisplayName("PUT /users/me - Should update user profile successfully")
	@WithMockUser(username = "john", roles = { "USER" })
	void updateMyProfile_shouldReturnUpdatedUser_whenValidData() throws Exception {
		// ===== ARRANGE =====
		UpdateUserRequest updateRequest = new UpdateUserRequest();
		updateRequest.setFullName("Updated Name");
		updateRequest.setEmail("updated@example.com");

		UserResponse updated = new UserResponse();
		updated.setUsername("john");
		updated.setFullname("Updated Name");
		updated.setEmail("updated@example.com");
		updated.setRole(List.of("USER"));
		updated.setId(1);

		when(userService.updateUser(eq("john"), any(UpdateUserRequest.class))).thenReturn(updated);

		// ===== ACT & ASSERT =====
		mockMvc.perform(put("/users/me").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.fullname").value("Updated Name"))
				.andExpect(jsonPath("$.data.email").value("updated@example.com"))
				.andExpect(jsonPath("$.code").value("OK"));

		verify(userService).updateUser(eq("john"), any(UpdateUserRequest.class));
	}

	@Test
	@DisplayName("PUT /users/me - Should return 400 BadRequest when email is invalid")
	@WithMockUser(username = "john", roles = { "USER" })
	void updateMyProfile_shouldReturnBadRequest_whenInvalidEmail() throws Exception {
		// ===== ARRANGE =====
		UpdateUserRequest updateRequest = new UpdateUserRequest();
		updateRequest.setFullName("John Doe");
		updateRequest.setEmail("not-an-email");

		// ===== ACT & ASSERT =====
		mockMvc.perform(put("/users/me").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"))
				.andExpect(jsonPath("$.message").value("Invalid email format"));
	}

	@Test
	@DisplayName("PUT /users/me - Should return 500 InternalServerError when email already exists")
	@WithMockUser(username = "john", roles = { "USER" })
	void updateMyProfile_shouldReturnBadRequest_whenEmailExists() throws Exception {
		// ===== ARRANGE =====
		UpdateUserRequest updateRequest = new UpdateUserRequest();
		updateRequest.setFullName("John Doe");
		updateRequest.setEmail("existing@example.com");

		when(userService.updateUser(eq("john"), any(UpdateUserRequest.class)))
				.thenThrow(new RuntimeException("Email already exists"));

		// ===== ACT & ASSERT =====
		mockMvc.perform(put("/users/me").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.code").value("INTERNAL_SERVER"))
				.andExpect(jsonPath("$.message").value("Email already exists"));
	}

	// === DELETE /users/{username} === (ADMIN only)
	@Test
	@DisplayName("DELETE /users/{username} - Should delete user successfully for ADMIN")
	@WithMockUser(username = "admin", roles = { "ADMIN" })
	void deleteUser_shouldReturnOk_whenAdmin() throws Exception {
		// ===== ARRANGE =====
		doNothing().when(userService).deleteUser("john");

		// ===== ACT & ASSERT =====
		mockMvc.perform(delete("/users/john")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data").value("Xóa Thành Công with UserNamejohn"))
				.andExpect(jsonPath("$.code").value("OK"));

		verify(userService).deleteUser("john");
	}

	// === GET /users - unauthorized (no ADMIN role) ===
	@Test
	@DisplayName("GET /users - Should return 403 Forbidden for non-ADMIN")
	@WithMockUser(username = "user", roles = { "USER" })
	void getAllUsers_shouldReturnForbidden_whenNonAdmin() throws Exception {
		// ===== ACT & ASSERT =====
		mockMvc.perform(get("/users")).andExpect(status().isForbidden());
	}

	// === DELETE /users/{username} - unauthorized (no ADMIN role) ===
	@Test
	@DisplayName("DELETE /users/{username} - Should return 403 Forbidden for non-ADMIN")
	@WithMockUser(username = "user", roles = { "USER" })
	void deleteUser_shouldReturnForbidden_whenNonAdmin() throws Exception {
		// ===== ACT & ASSERT =====
		mockMvc.perform(delete("/users/john")).andExpect(status().isForbidden());
	}
}
