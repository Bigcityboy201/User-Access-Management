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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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
	@WithMockUser(username = "admin", roles = { "ADMIN" })
	void getAllUsers_shouldReturnListOfUsers() throws Exception {
		Role adminRole = Role.builder().id(1).roleName("ADMIN").build();
		Role userRole = Role.builder().id(2).roleName("USER").build();

		List<User> mockUsers = List.of(
				User.builder().id(1).username("admin").fullname("Admin").email("admin@example.com")
						.roles(List.of(adminRole)).build(),
				User.builder().id(2).username("jane").fullname("Jane Smith").email("jane@example.com")
						.roles(List.of(userRole)).build());

		when(userService.getAllUsers()).thenReturn(mockUsers);

		mockMvc.perform(get("/users"))
				.andDo(print()) // Debug: print response để xem format thực tế
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(2)))
				.andExpect(jsonPath("$.data[0].username").value("admin"))
				.andExpect(jsonPath("$.data[1].username").value("jane"))
				.andExpect(jsonPath("$.code").value("OK"));

		verify(userService).getAllUsers();
	}

	// === GET /users/me ===
	@Test
	@WithMockUser(username = "john", roles = { "USER" })
	void getMyProfile_shouldReturnUserProfile() throws Exception {
		UserResponse mockResponse = new UserResponse();
		mockResponse.setUsername("john");
		mockResponse.setFullname("John Doe");
		mockResponse.setEmail("john@example.com");
		mockResponse.setRole(List.of("USER"));
		mockResponse.setId(1);

		when(userService.getUserByUsername("john")).thenReturn(mockResponse);

		mockMvc.perform(get("/users/me")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.username").value("john"))
				.andExpect(jsonPath("$.data.fullname").value("John Doe"))
				.andExpect(jsonPath("$.data.email").value("john@example.com"))
				.andExpect(jsonPath("$.code").value("OK"));

		verify(userService).getUserByUsername("john");
	}

	// === PUT /users/me ===
	@Test
	@WithMockUser(username = "john", roles = { "USER" })
	void updateMyProfile_shouldUpdateUser() throws Exception {
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

		mockMvc.perform(put("/users/me").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.fullname").value("Updated Name"))
				.andExpect(jsonPath("$.data.email").value("updated@example.com"))
				.andExpect(jsonPath("$.code").value("OK"));

		verify(userService).updateUser(eq("john"), any(UpdateUserRequest.class));
	}

	// === DELETE /users/{username} === (ADMIN only)
	@Test
	@WithMockUser(username = "admin", roles = { "ADMIN" })
	void deleteUser_shouldReturnOk() throws Exception {
		// Setup
		doNothing().when(userService).deleteUser("john");

		// Execute & Verify - controller trả 200 OK với SuccessResponse wrapper
		mockMvc.perform(delete("/users/john")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data").value("Xóa Thành Công with UserNamejohn"))
				.andExpect(jsonPath("$.code").value("OK"));

		verify(userService).deleteUser("john");
	}

	// === GET /users - unauthorized (no ADMIN role) ===
	@Test
	@WithMockUser(roles = { "USER" })
	void getAllUsers_shouldReturnForbiddenForNonAdmin() throws Exception {
		mockMvc.perform(get("/users")).andExpect(status().isForbidden());
	}

	// === DELETE /users/{username} - unauthorized (no ADMIN role) ===
	@Test
	@WithMockUser(username = "user", roles = { "USER" })
	void deleteUser_shouldReturnForbiddenForNonAdmin() throws Exception {
		mockMvc.perform(delete("/users/john")).andExpect(status().isForbidden());
	}

}
