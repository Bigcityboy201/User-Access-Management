package com.r2s.user.dto;

import java.util.List;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

		@NotBlank(message = "Email không được bỏ trống")
		@Email(message = "Invalid email format")
		@Column(nullable = false, unique = true)
		private String email;

		@NotBlank(message = "Họ tên không được bỏ trống")
		@Size(min = 2, max = 50, message = "Họ tên phải từ 2 đến 50 ký tự")
		@Column(nullable = false)
		private String fullName;
	}

}
