package com.utilitybilling.consumerservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateConsumerRequest {

	@NotBlank
	private String fullName;

	@NotBlank
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
