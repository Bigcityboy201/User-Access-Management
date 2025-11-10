package com.r2s.user.service.IMPL;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.r2s.core.dto.CreateUserProfileDTO;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.RoleRepository;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.service.UserProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceIMPL implements UserProfileService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	@Override
	@Transactional
	public void create(CreateUserProfileDTO dto) {
		log.info("Creating user profile: username={}, email={}, fullName={}, roleNames={}", dto.getUsername(),
				dto.getEmail(), dto.getFullName(), dto.getRoleNames());

		if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
			log.warn("User with username {} already exists, skipping creation", dto.getUsername());
			return;
		}

		// Create new user profile
		User user = User.builder().username(dto.getUsername()).email(dto.getEmail()).fullname(dto.getFullName())
				.password("N/A") // Dummy password - user-service doesn't handle authentication
				.deleted(false).build();

		// Set roles if provided
		if (dto.getRoleNames() != null && !dto.getRoleNames().isEmpty()) {
			List<Role> roles = new ArrayList<>();
			for (String roleName : dto.getRoleNames()) {
				roleRepository.findByRoleName(roleName).ifPresentOrElse(roles::add,
						() -> log.warn("Role '{}' not found in database for user '{}', skipping role assignment",
								roleName, dto.getUsername()));
			}
			if (!roles.isEmpty()) {
				user.setRoles(roles);
				log.info("Assigned {} roles to user: {}", roles.size(), roles.stream().map(Role::getRoleName).toList());
			} else {
				log.warn("No valid roles found for user '{}' (requested roles: {}), user will be created without roles",
						dto.getUsername(), dto.getRoleNames());
			}
		}

		User savedUser = userRepository.save(user);
		log.info("User profile created successfully with ID: {} for username: {} with roles: {}", savedUser.getId(),
				savedUser.getUsername(),
				savedUser.getRoles() != null ? savedUser.getRoles().stream().map(Role::getRoleName).toList() : "none");
	}
}
