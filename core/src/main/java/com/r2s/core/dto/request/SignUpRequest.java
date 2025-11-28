package com.r2s.core.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpRequest {
	@NotBlank(message = "username must not be blank")
	private String username;

	@NotBlank(message = "password must not be blank")
	@Size(min = 6, message = "password must be at least 6 characters")
	private String password;

	@JsonProperty("email")
	@NotBlank(message = "email must not be blank")
	@Email(message = "email must be a valid email")
	private String email;

	@JsonProperty("fullName")
	@NotBlank(message = "fullName must not be blank")
	private String fullName;

	private RoleRequest role;

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class RoleRequest {
		private String roleName;
	}
}
