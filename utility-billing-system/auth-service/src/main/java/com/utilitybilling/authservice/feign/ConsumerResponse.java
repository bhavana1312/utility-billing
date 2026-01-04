package com.utilitybilling.authservice.feign;

import lombok.Data;

@Data
public class ConsumerResponse {
	private String id;
	private String fullName;
	private String email;

}
