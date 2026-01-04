package com.utilitybilling.meterservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "consumer-service")
public interface ConsumerClient {

	@GetMapping("/consumers/username/{username}")
	ConsumerResponse getByUsername(@PathVariable("username") String username);
	
	@GetMapping("/consumers/{id}")
	ConsumerResponse get(@PathVariable String id);
}
