package com.utilitybilling.authservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "consumer-service", fallback = ConsumerClientFallback.class)
public interface ConsumerClient {

	@GetMapping("/consumers/username/{username}")
	ConsumerResponse getByUsername(@PathVariable("username") String username);
}
