package com.r2s.user.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.r2s.core.entity.User;
import com.r2s.core.response.SuccessResponse;
import com.r2s.user.dto.UserResponse;
import com.r2s.user.dto.UserResponse.UpdateUserRequest;
import com.r2s.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
	private final UserService userService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public SuccessResponse<List<User>> getAllUsers() {
		return SuccessResponse.of(userService.getAllUsers());
	}

	@GetMapping("/me")
	public SuccessResponse<UserResponse> getMyProfile(@Valid Authentication authentication) {
		String username = authentication.getName();
		return SuccessResponse.of(userService.getUserByUsername(username));
	}

	@PutMapping("/me")
	public SuccessResponse<UserResponse> updateMyProfile(@Valid @RequestBody UpdateUserRequest request,
			Authentication authentication) {
		String username = authentication.getName();
		return SuccessResponse.of(userService.updateUser(username, request));
	}

	@DeleteMapping("/{username}")
	@PreAuthorize("hasRole('ADMIN')")
	public SuccessResponse<String> deleteUser(@Valid @PathVariable("username") String username) {
		log.info("DELETE /users/{} requested", username);
		try {
			userService.deleteUser(username);
			log.info("DELETE /users/{} succeeded", username);
			return SuccessResponse.of("Xóa Thành Công with UserName" + username);
		} catch (Exception e) {
			log.error("DELETE /users/{} failed: {}", username, e.getMessage(), e);
			throw e;
		}
	}
}
