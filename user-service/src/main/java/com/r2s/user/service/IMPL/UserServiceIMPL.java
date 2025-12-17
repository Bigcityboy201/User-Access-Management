package com.r2s.user.service.IMPL;

import java.util.List;

import org.springframework.stereotype.Service;

import com.r2s.core.entity.User;
import com.r2s.core.exception.UserNotFoundException;
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

	// bổ sung check user is deleted...
	@Override
	public UserResponse getUserByUsername(String username) {
		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		return UserResponse.fromEntity(user);
	}

	// bổ sung lỗi email trùng
	// check user is deleted
	// validation input(if cho từng field)
	// ...
	@Override
	public UserResponse updateUser(String username, UpdateUserRequest req) {
		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UserNotFoundException("User Not Found"));

		// Update fullname if provided
		if (req.getFullName() != null && !req.getFullName().trim().isEmpty()) {
			user.setFullname(req.getFullName());
		}

		// Update email if provided and different from current email
		if (req.getEmail() != null && !req.getEmail().trim().isEmpty()) {
			// Check if email is different from current email
			if (!req.getEmail().equals(user.getEmail())) {
				// Check if email already exists for another user
				this.userRepository.findByEmail(req.getEmail()).ifPresent(existingUser -> {
					if (!existingUser.getUsername().equals(username)) {
						throw new UserNotFoundException("Email already exists: " + req.getEmail());
					}
				});
				user.setEmail(req.getEmail());
			}
		}

		// Persist the updated user but rely on the in-memory instance for response
		userRepository.save(user);
		return UserResponse.fromEntity(user);
	}

	// thêm check user isDeleted
	// bắt lỗi DeleteException->để trả 400
	// ...
	@Override
	public void deleteUser(String username) {
		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UserNotFoundException("User Not Found"));
		userRepository.delete(user);
	}
}
