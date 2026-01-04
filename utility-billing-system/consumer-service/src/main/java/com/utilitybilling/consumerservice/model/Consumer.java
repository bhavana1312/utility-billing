package com.utilitybilling.consumerservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.*;
import java.time.Instant;

@Document(collection = "consumers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consumer {

	@Id
	private String id;

	@NotBlank(message = "Full name must not be empty")
	@Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
	private String fullName;

	@NotBlank(message = "Email must not be empty")
	@Email(message = "Email must be a valid email address")
	private String email;

	@NotBlank(message = "Phone number must not be empty")
	@Pattern(regexp = "^\\d{10,15}$", message = "Phone number must contain 10 to 15 digits")
	private String phone;

	@NotBlank(message = "Address line must not be empty")
	private String addressLine1;

	@NotBlank(message = "City must not be empty")
	private String city;

	@NotBlank(message = "State must not be empty")
	private String state;

	@NotBlank(message = "Postal code must not be empty")
	@Pattern(regexp = "^[0-9A-Za-z\\- ]{4,10}$", message = "Postal code format is invalid")
	private String postalCode;

	private boolean active;

	private Instant createdAt;
	private Instant updatedAt;
}
