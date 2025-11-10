package com.r2s.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.r2s.core.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {
	@GetMapping("/user")
	@PreAuthorize("hasRole('USER')")
	public SuccessResponse<String> userAccess() {
		return SuccessResponse.of("Hello User");
	}

	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public SuccessResponse<String> adminAccess() {
		return SuccessResponse.of("Hello Admin");
	}

	@GetMapping("/mod")
	@PreAuthorize("hasRole('MODERATOR')")
	public SuccessResponse<String> moderatorAccess() {
		return SuccessResponse.of("Hello MODERATOR");
	}
}
