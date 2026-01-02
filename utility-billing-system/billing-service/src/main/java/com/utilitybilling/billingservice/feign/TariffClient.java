package com.utilitybilling.billingservice.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="tariff-service")
public interface TariffClient{

    @CircuitBreaker(name="tariffService",fallbackMethod="fallback")
    @GetMapping("/utilities/tariffs/{utilityType}/plans/{plan}")
    TariffResponse getActive(@PathVariable("utilityType") String utilityType, @PathVariable("plan") String plan);

    default TariffResponse fallback(String utilityType,Throwable t){
        throw new IllegalStateException("Tariff service unavailable");
    }
}
