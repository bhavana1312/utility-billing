package com.utilitybilling.paymentservice.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "billing-service")
public interface BillingClient {

	@GetMapping("/billing/internal/{billId}")
	BillResponse getBill(@PathVariable("billId") String billId);

	@PutMapping("/billing/internal/{billId}/mark-paid")
	@CircuitBreaker(name = "billingCB", fallbackMethod = "fallback")
	void markPaid(@PathVariable("billId") String billId);

	@GetMapping("/billing/internal/consumer/{consumerId}/outstanding")
	@CircuitBreaker(name = "billingCB", fallbackMethod = "fallback")
	OutstandingBalanceResponse outstanding(@PathVariable("consumerId") String consumerId);

	default void fallback(String billId, Throwable t) {
		throw new IllegalStateException("Billing service unavailable");
	}
}
