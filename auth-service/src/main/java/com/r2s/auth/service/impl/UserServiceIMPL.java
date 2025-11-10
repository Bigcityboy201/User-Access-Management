package com.r2s.auth.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
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
import com.r2s.core.entity.User;
import com.r2s.core.exception.UserAlreadyExistException;
import com.r2s.core.repository.RoleRepository;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.security.CustomUserDetails;
import com.r2s.core.util.JwtUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceIMPL implements UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RoleRepository roleRepository;
	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;
	private final UserKafkaProducer producer;
	@Value("${jwt.duration}")
	private long jwtDuration;

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
		// Set role from request, default to USER if not provided
		String roleName = (request.getRole() != null && request.getRole().getRoleName() != null)
				? request.getRole().getRoleName()
				: SecurityRole.ROLE_USER;
		this.roleRepository.findByRoleName(roleName).ifPresent(role -> {
			user.setRoles(List.of(role));
		});

		User savedUser;
		try {
			savedUser = this.userRepository.save(user);
		} catch (Exception e) {
			System.err.println("Error saving user: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
		// Extract role names from savedUser
		List<String> roleNames = savedUser.getRoles().stream().map(role -> role.getRoleName()).toList();

		// Send event to Kafka for user-service
		CreateUserProfileDTO event = CreateUserProfileDTO.builder().userId(savedUser.getId())
				.username(savedUser.getUsername()).email(savedUser.getEmail()).fullName(savedUser.getFullname())
				.roleNames(roleNames).build();
		producer.sendUserRegistered(event);

		return true;
	}

	@Override
	public SignInResponse signIn(SignInRequest request) {
		// Authenticate user
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		// Get user details
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

		// Generate JWT token
		String token = jwtUtils.generateToken(userDetails);

		// Calculate expiration date using jwt.duration from config
		java.util.Date expirationDate = new java.util.Date(System.currentTimeMillis() + 1000 * jwtDuration);

		return SignInResponse.builder().token(token).expiredDate(expirationDate).build();
	}
}
