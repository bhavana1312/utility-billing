package com.utilitybilling.billingservice.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="consumer-service")
public interface ConsumerClient{

    @CircuitBreaker(name="consumerService",fallbackMethod="fallback")
    @GetMapping("/consumers/{id}/exists")
    boolean exists(@PathVariable String id);

    default boolean fallback(String id,Throwable t){
        throw new IllegalStateException("Consumer service unavailable");
    }
}
