package com.utilitybilling.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
	@NotBlank
	private String resetToken;
	@NotBlank
	private String newPassword;
}
