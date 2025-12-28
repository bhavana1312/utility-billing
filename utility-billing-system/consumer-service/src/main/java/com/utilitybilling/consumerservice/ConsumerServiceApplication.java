package com.utilitybilling.consumerservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.utilitybilling.consumerservice.client")
public class ConsumerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerServiceApplication.class, args);
	}

	@Autowired
	private ApplicationContext context;

	@PostConstruct
	public void debugContext() {
		System.out.println("Feign beans:");
		for (String name : context.getBeanDefinitionNames()) {
			if (name.toLowerCase().contains("feign"))
				System.out.println(" - " + name);
		}
	}
	
	@PostConstruct
	public void checkAuthClient(){
	    try{
	        Object bean=context.getBean("authClient");
	        System.out.println("AuthClient bean FOUND: "+bean);
	    }catch(Exception e){
	        System.out.println("AuthClient bean NOT FOUND");
	    }
	}

}
