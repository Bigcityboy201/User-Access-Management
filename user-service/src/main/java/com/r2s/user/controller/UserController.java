package com.r2s.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
import com.r2s.user.dto.UserResponse;
import com.r2s.user.dto.UserResponse.UpdateUserRequest;
import com.r2s.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<User>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
		String username = authentication.getName();
		return ResponseEntity.ok(userService.getUserByUsername(username));
	}

	@PutMapping("/me")
	public ResponseEntity<UserResponse> updateMyProfile(@RequestBody UpdateUserRequest request,
			Authentication authentication) {
		String username = authentication.getName();
		return ResponseEntity.ok(userService.updateUser(username, request));
	}

    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<Void> deleteUser(@PathVariable("username") String username) {
		userService.deleteUser(username);
		return ResponseEntity.noContent().build();
	}
}
