package com.r2s.user.service;

import java.util.List;

import com.r2s.core.entity.User;
import com.r2s.user.dto.UserResponse;
import com.r2s.user.dto.UserResponse.UpdateUserRequest;

public interface UserService {

	List<User> getAllUsers();

	UserResponse getUserByUsername(final String username);

	UserResponse updateUser(String username, UpdateUserRequest req);

	void deleteUser(String username);
}
