package com.utilitybilling.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

	@NotBlank(message = "Username is required")
	private String username;

	@NotBlank(message = "Old password is required")
	private String oldPassword;

	@NotBlank(message = "New password is required")
	@Size(min = 8, message = "New password must be at least 8 characters")
	private String newPassword;
}
