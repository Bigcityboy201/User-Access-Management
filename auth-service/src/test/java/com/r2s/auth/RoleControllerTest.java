package com.r2s.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackages = { "com.r2s.auth", "com.r2s.core.handler" })
class RoleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	// 1. userAccess_shouldReturnForbiddenWhenNoAuth
	@Test
	@DisplayName("GET /role/user - Should return 403 when no authentication")
	void userAccess_shouldReturnForbiddenWhenNoAuth() throws Exception {
		// Act
		ResultActions response = mockMvc.perform(get("/role/user"));

		// Assert
		response.andExpect(status().isUnauthorized());
	}

	// 2. userAccess_shouldReturnForbiddenWhenAdminRole
	@Test
	@WithMockUser(username = "admin", roles = { "ADMIN" })
	@DisplayName("GET /role/user - Should return 403 when user has ADMIN role")
	void userAccess_shouldReturnForbiddenWhenAdminRole() throws Exception {
		// Act
		ResultActions response = mockMvc.perform(get("/role/user"));

		// Assert
		response.andExpect(status().isForbidden());
	}

	// 3. adminAccess_shouldReturnForbiddenWhenUserRole
	@Test
	@WithMockUser(username = "user", roles = { "USER" })
	@DisplayName("GET /role/admin - Should return 403 when user has USER role")
	void adminAccess_shouldReturnForbiddenWhenUserRole() throws Exception {
		// Act
		ResultActions response = mockMvc.perform(get("/role/admin"));

		// Assert
		response.andExpect(status().isForbidden());
	}

	// 4. adminAccess_shouldReturnOkWhenAdminRole
	@Test
	@WithMockUser(username = "admin", roles = { "ADMIN" })
	@DisplayName("GET /role/admin - Should return 200 and correct message when user has ADMIN role")
	void adminAccess_shouldReturnOkWhenAdminRole() throws Exception {
		// Act
		ResultActions response = mockMvc.perform(get("/role/admin"));

		// Assert
		response.andExpect(status().isOk()).andExpect(jsonPath("$.data").value("Hello Admin"));
	}

	// 5. moderatorAccess_shouldReturnForbiddenWhenUserRole
	@Test
	@WithMockUser(username = "user", roles = { "USER" })
	@DisplayName("GET /role/mod - Should return 403 when user has USER role")
	void moderatorAccess_shouldReturnForbiddenWhenUserRole() throws Exception {
		// Act
		ResultActions response = mockMvc.perform(get("/role/mod"));

		// Assert
		response.andExpect(status().isForbidden());
	}

	// 6. moderatorAccess_shouldReturnOkWhenModeratorRole
	@Test
	@WithMockUser(username = "moderator", roles = { "MODERATOR" })
	@DisplayName("GET /role/mod - Should return 200 and correct message when user has MODERATOR role")
	void moderatorAccess_shouldReturnOkWhenModeratorRole() throws Exception {
		// Act
		ResultActions response = mockMvc.perform(get("/role/mod"));

		// Assert
		response.andExpect(status().isOk()).andExpect(jsonPath("$.data").value("Hello MODERATOR"));
	}
}


