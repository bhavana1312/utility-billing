package com.utilitybilling.consumerservice.feign;

import com.utilitybilling.consumerservice.dto.CreateAuthUserRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "auth-service")
public interface AuthClient {

	@PostMapping("/internal/users")
	void createUser(CreateAuthUserRequest request);
}
