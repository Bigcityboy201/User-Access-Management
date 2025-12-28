package com.r2s.auth.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.r2s.auth.kafka.UserKafkaProducer;
import com.r2s.auth.service.UserService;
import com.r2s.core.constant.SecurityRole;
import com.r2s.core.dto.CreateUserProfileDTO;
import com.r2s.core.dto.request.SignInRequest;
import com.r2s.core.dto.request.SignUpRequest;
import com.r2s.core.dto.response.SignInResponse;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.exception.UserAlreadyExistException;
import com.r2s.core.repository.RoleRepository;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.security.CustomUserDetails;
import com.r2s.core.util.JwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j

public class UserServiceIMPL implements UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RoleRepository roleRepository;
	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;
	private final UserKafkaProducer producer;

	@Value("${jwt.duration}")
	private long jwtDuration;

	// bổ sung check email->tồn tại
	@Override
	public Boolean signUp(SignUpRequest request) {
		// check exists userName
		this.userRepository.findByUsername(request.getUsername()).ifPresent((u) -> {
			throw new UserAlreadyExistException("User already exist!" + request.getUsername());
		});

		// Create new User entity
		User user = User.builder().username(request.getUsername())
				.password(this.passwordEncoder.encode(request.getPassword())).email(request.getEmail())
				.fullname(request.getFullName()).deleted(false).build();

		// Determine role name: request role if provided, otherwise default USER role
		String roleName = (request.getRole() != null && request.getRole().getRoleName() != null)
				? request.getRole().getRoleName()
				: SecurityRole.ROLE_USER;
		if (!roleName.equals(SecurityRole.ROLE_USER) && !roleName.equals(SecurityRole.ROLE_ADMIN)
				&& !roleName.equals(SecurityRole.ROLE_MODERATOR)) {
			throw new RuntimeException("Invalid role: " + roleName);
		}

		// Tìm role trong DB
		var roleOpt = roleRepository.findByRoleName(roleName);
		Role role;
		if (roleOpt.isEmpty()) {
			// Nếu chưa có trong DB, tạo mới role
			role = Role.builder().roleName(roleName).description(roleName + " role").build();
			role = roleRepository.save(role);
		} else {
			role = roleOpt.get();
		}

		// Gán role cho user
		user.setRoles(List.of(role));

		User savedUser;
		try {
			savedUser = this.userRepository.save(user);
		} catch (Exception e) {
			// Log lỗi hệ thống nhưng KHÔNG nuốt lỗi → để GlobalExceptionHandler đẩy 500
			log.error("Failed to save user {}: {}", request.getUsername(), e.getMessage(), e);
			throw e; // bubble lên 500
		}

		// Kafka event – use the in-memory user roles so that mocks that return null
		// from save() in unit tests don't break this logic
		List<String> roleNames = user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList());

		// Send event to Kafka for user-service
		CreateUserProfileDTO event = CreateUserProfileDTO.builder().userId(savedUser != null ? savedUser.getId() : null)
				.username(user.getUsername()).email(user.getEmail()).fullName(user.getFullname()).roleNames(roleNames)
				.build();
		producer.sendUserRegistered(event);

		return true;
	}

	// bổ sung check userNotFoundException->(lỗi:userNotFound,user is deleted)
	@Override
	public SignInResponse signIn(SignInRequest request) {
		// Authenticate user
		Authentication authentication;
		try {
			authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
		} catch (BadCredentialsException ex) {
			// Log nhẹ, không leak info
			log.warn("Failed login attempt for username: {}", request.getUsername());
			throw ex; // Cho GlobalExceptionHandler trả về 401
		}

		// Get user details
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		// Generate JWT token
		String token = jwtUtils.generateToken(userDetails);

		// Calculate expiration date using jwt.duration from config
		log.info("User {} logged in. Token hash: {}", request.getUsername(), token.hashCode());

		java.util.Date expirationDate = new java.util.Date(System.currentTimeMillis() + (jwtDuration * 1000));
		return SignInResponse.builder().token(token).expiredDate(expirationDate).build();
	}
}
