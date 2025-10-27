package com.r2s.user.dto;

import java.util.List;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
	private String username;
	private List<String> role;
	private Integer id;

	public static UserResponse fromEntity(final User user) {
		UserResponse res = new UserResponse();
		res.setUsername(user.getUsername());
		res.setRole(user.getRoles().stream().map(Role::getRoleName).toList());
		res.setId(user.getId());
		return res;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class UpdateUserRequest {
	}
}
