package com.r2s.core.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	private String username;
	private String password;

	@JsonProperty("email")
	private String email;

	@JsonProperty("fullName")
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
