package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.feign.TariffClient;
import com.utilitybilling.billingservice.feign.TariffResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
public class TariffGateway {

	private final TariffClient client;

	public TariffGateway(TariffClient client) {
		this.client = client;
	}

	@CircuitBreaker(name = "tariff-service", fallbackMethod = "getActiveFallback")
	public TariffResponse getActive(String utilityType, String plan) {
		return client.getActive(utilityType, plan);
	}

	public TariffResponse getActiveFallback(String utilityType, String plan, Throwable t) {
		throw new IllegalStateException("Tariff service unavailable");
	}
}
