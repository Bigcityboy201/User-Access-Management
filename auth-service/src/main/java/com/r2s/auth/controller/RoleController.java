package com.r2s.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {
	@GetMapping("/user")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<String> userAccess() {
		return ResponseEntity.ok("Hello User");
	}

	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> adminAccess() {
		return ResponseEntity.ok("Hello Admin");
	}

	@GetMapping("/mod")
	@PreAuthorize("hasRole('MODERATOR')")
	public ResponseEntity<String> moderatorAccess() {
		return ResponseEntity.ok("Hello MODERATOR");
	}
}
