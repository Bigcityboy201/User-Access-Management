package com.r2s.core.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class SignInRequest {

	@NotBlank(message = "username must not be blank")
	private String username;

	@NotBlank(message = "password must not be blank")
	private String password;
}










