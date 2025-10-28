package com.r2s.user.service.IMPL;

import java.util.List;

import org.springframework.stereotype.Service;

import com.r2s.core.entity.User;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.dto.UserResponse;
import com.r2s.user.dto.UserResponse.UpdateUserRequest;
import com.r2s.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceIMPL implements UserService {
	private final UserRepository userRepository;

	@Override
	public List<User> getAllUsers() {
		return this.userRepository.findAll();
	}

	@Override
	public UserResponse getUserByUsername(String username) {
		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found"));

		return UserResponse.fromEntity(user);
	}

	@Override
	public UserResponse updateUser(String username, UpdateUserRequest req) {
		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User Not Found"));
		// Update logic will be added when needed
		user.setFullname(req.getFullName());
		user.setEmail(req.getEmail());
		return UserResponse.fromEntity(userRepository.save(user));
	}

	@Override
	public void deleteUser(String username) {
		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User Not Found"));
		userRepository.delete(user);
	}
}
