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
	private String fullname;
	private String email;
	private List<String> role;
	private Integer id;

	public static UserResponse fromEntity(final User user) {
		UserResponse res = new UserResponse();
		res.setUsername(user.getUsername());
		res.setRole(user.getRoles().stream().map(Role::getRoleName).toList());
		res.setFullname(user.getFullname());
		res.setId(user.getId());
		res.setEmail(user.getEmail());
		return res;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UpdateUserRequest {
		private String email;
		private String fullName;

		public static UpdateUserRequest fromEntity(final User user) {
			UpdateUserRequest res = new UpdateUserRequest();
			res.setEmail(user.getEmail());
			res.setFullName(user.getFullname());
			return res;
		}
	}
}
