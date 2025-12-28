package com.utilitybilling.consumerservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
public class CreateConsumerRequest {

	@NotBlank(message = "Full name is required")
	private String fullName;

	@Email(message = "Invalid email")
	@NotBlank
	private String email;

	@NotBlank(message = "Phone is required")
	private String phone;

	@NotBlank
	private String addressLine1;

	@NotBlank
	private String city;

	@NotBlank
	private String state;

	@NotBlank
	private String postalCode;
}
