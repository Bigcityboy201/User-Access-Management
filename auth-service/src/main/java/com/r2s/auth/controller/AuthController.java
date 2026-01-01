package com.r2s.auth.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.r2s.auth.service.UserService;
import com.r2s.core.dto.request.SignInRequest;
import com.r2s.core.dto.request.SignUpRequest;
import com.r2s.core.dto.response.SignInResponse;
import com.r2s.core.response.SuccessResponse;
import com.r2s.core.util.JwtUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/auth")
public class AuthController {

	// 12345
	private final UserService userService;
	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;

	@PostMapping(path = "/register")
	public SuccessResponse<String> register(@Valid @RequestBody SignUpRequest request) {
		userService.signUp(request);
		return SuccessResponse.of("User registered successfully");
	}

	@PostMapping("/login")
	public SuccessResponse<SignInResponse> login(@Valid @RequestBody SignInRequest request) {
		return SuccessResponse.of(userService.signIn(request));
	}
}