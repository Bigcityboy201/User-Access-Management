package com.r2s.auth.controller;

import com.r2s.auth.dto.request.SignInRequest;
import com.r2s.auth.dto.request.SignUpRequest;
import com.r2s.auth.dto.response.SignInResponse;
import com.r2s.auth.service.UserService;
import com.r2s.auth.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping(path = "/register")
    public ResponseEntity<String> register(@RequestBody SignUpRequest request) {
        userService.signUp(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<SignInResponse> login(@RequestBody SignInRequest request) {
         return ResponseEntity.ok(userService.signIn(request));
    }
}