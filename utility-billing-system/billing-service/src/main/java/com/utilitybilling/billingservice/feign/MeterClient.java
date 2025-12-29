package com.utilitybilling.billingservice.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "meter-service")
public interface MeterClient {

	@CircuitBreaker(name = "meterService", fallbackMethod = "meterFallback")
	@GetMapping("/meters/{meterNumber}")
	MeterResponse getMeter(@PathVariable("meterNumber") String meterNumber);

	@CircuitBreaker(name = "meterService", fallbackMethod = "readingFallback")
	@GetMapping("/meters/{meterNumber}/last-reading")
	double getLastReading(@PathVariable("meterNumber") String meterNumber);

	default MeterResponse meterFallback(String meterNumber, Throwable t) {
		throw new IllegalStateException("Meter service unavailable");
	}

	default double readingFallback(String meterNumber, Throwable t) {
		throw new IllegalStateException("Meter service unavailable");
	}
}
