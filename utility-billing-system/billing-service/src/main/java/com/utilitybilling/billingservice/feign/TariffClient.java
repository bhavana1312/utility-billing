package com.utilitybilling.billingservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tariff-service")
public interface TariffClient {

	@GetMapping("/utilities/tariffs/{utilityType}/plans/{plan}")
	TariffResponse getActive(@PathVariable String utilityType, @PathVariable String plan);
}
