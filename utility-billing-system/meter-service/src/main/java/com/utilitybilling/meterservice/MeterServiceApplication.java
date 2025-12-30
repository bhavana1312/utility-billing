package com.utilitybilling.meterservice;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableFeignClients
public class MeterServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeterServiceApplication.class, args);
	}

}
