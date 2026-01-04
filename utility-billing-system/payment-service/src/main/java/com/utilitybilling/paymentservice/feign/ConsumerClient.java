package com.utilitybilling.paymentservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "consumer-service")
public interface ConsumerClient {

	@GetMapping("/consumers/{id}")
	ConsumerResponse get(@PathVariable("id") String id);
}
