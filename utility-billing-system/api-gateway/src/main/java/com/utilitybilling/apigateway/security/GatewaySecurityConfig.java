package com.utilitybilling.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class GatewaySecurityConfig {

	private final JwtAuthenticationFilter jwtFilter;

	public GatewaySecurityConfig(JwtAuthenticationFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}

	@Bean
	@Order(0)
	public SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {
		return http.securityMatcher("/actuator/**").csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
	}

	@Bean
	@Order(1)
	public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
		return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth

				// Public auth endpoints
				.requestMatchers("/auth/**").permitAll()

				.requestMatchers("/internal/**").permitAll()
				
				.requestMatchers(HttpMethod.GET, "/utilities/tariffs/**").permitAll()

				// Public consumer onboarding
				.requestMatchers(HttpMethod.POST, "/consumer-requests").permitAll()

				// Tariff – Admin only
				.requestMatchers(HttpMethod.POST, "/utilities/tariffs/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.PUT, "/utilities/tariffs/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/utilities/tariffs/**").hasRole("ADMIN")

				// Consumer admin actions
				.requestMatchers(HttpMethod.POST, "/consumers/from-request/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.PUT, "/consumers/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/consumers/**").hasRole("ADMIN")

				// Consumer read
				.requestMatchers(HttpMethod.GET, "/consumers/**").hasAnyRole("USER","ADMIN","BILLING_OFFICER")

				// Consumer request review (ADMIN)
				.requestMatchers(HttpMethod.GET, "/consumer-requests/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.PUT, "/consumer-requests/**").hasRole("ADMIN")

				// Public connection request
				.requestMatchers(HttpMethod.POST, "/meters/connection-requests").permitAll()

				// Admin approval / rejection
				.requestMatchers(HttpMethod.POST, "/meters/connection-requests/**").hasRole("ADMIN")

				// Meter readings — BILLING OFFICER ONLY
				.requestMatchers(HttpMethod.POST, "/meters/readings").hasRole("BILLING_OFFICER")
				
				// BILLING SERVICE
				.requestMatchers(HttpMethod.POST, "/billing/generate").hasRole("BILLING_OFFICER")
				.requestMatchers(HttpMethod.GET, "/billing/**").hasAnyRole("USER","ADMIN","BILLING_OFFICER","ACCOUNTS_OFFICER")
				
				// PAYMENTS
				.requestMatchers(HttpMethod.POST, "/payments/initiate").hasRole("USER")
				.requestMatchers(HttpMethod.POST, "/payments/confirm").hasRole("USER")
				.requestMatchers(HttpMethod.POST, "/payments/offline").hasRole("ADMIN")
				

				// Meter read APIs
				.requestMatchers(HttpMethod.GET, "/meters/**").authenticated()

				.anyRequest().authenticated()).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

}
