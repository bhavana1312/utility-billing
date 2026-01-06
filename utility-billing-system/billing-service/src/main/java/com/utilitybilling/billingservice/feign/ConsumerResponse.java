package com.utilitybilling.billingservice.feign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsumerResponse {
	private String id;
	private String fullName;
	private String email;
	private String phone;
	private boolean active;
}
