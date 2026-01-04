package com.utilitybilling.authservice.feign;

import org.springframework.stereotype.Component;

import com.utilitybilling.authservice.exception.DownstreamServiceException;

@Component
public class ConsumerClientFallback implements ConsumerClient{

	@Override
	public ConsumerResponse getByUsername(String username){
		throw new DownstreamServiceException("Consumer service unavailable");
	}
}
