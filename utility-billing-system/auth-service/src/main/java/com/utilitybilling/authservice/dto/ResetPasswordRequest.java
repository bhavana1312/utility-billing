package com.utilitybilling.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
	@NotBlank(message = "Reset Token is required")
	private String resetToken;
	@NotBlank(message = "Password is required")
	private String newPassword;
}
