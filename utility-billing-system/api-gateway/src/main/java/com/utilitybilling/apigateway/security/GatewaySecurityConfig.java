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
		return http.cors().and().csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth

				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

				.requestMatchers("/auth/**").permitAll().requestMatchers("/internal/**").permitAll()

				.requestMatchers(HttpMethod.GET, "/utilities/tariffs/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/consumer-requests").permitAll()

				.requestMatchers(HttpMethod.POST, "/utilities/tariffs/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.PUT, "/utilities/tariffs/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/utilities/tariffs/**").hasRole("ADMIN")

				.requestMatchers(HttpMethod.POST, "/consumers/from-request/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.PUT, "/consumers/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/consumers/**").hasRole("ADMIN")

				.requestMatchers(HttpMethod.GET, "/consumers/**").hasAnyRole("USER", "ADMIN", "BILLING_OFFICER", "ACCOUNTS_OFFICER")

				.requestMatchers(HttpMethod.GET, "/consumer-requests/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.PUT, "/consumer-requests/**").hasRole("ADMIN")

				.requestMatchers(HttpMethod.POST, "/meters/connection-requests").permitAll()
				.requestMatchers(HttpMethod.POST, "/meters/connection-requests/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.GET, "meters/consumer/**").hasAnyRole("USER", "ADMIN", "BILLING_OFFICER", "ACCOUNTS_OFFICER")

				.requestMatchers(HttpMethod.POST, "/meters/readings").hasRole("BILLING_OFFICER")

				.requestMatchers(HttpMethod.POST, "/billing/generate").hasRole("BILLING_OFFICER")
				.requestMatchers(HttpMethod.GET, "/billing/**")
				.hasAnyRole("USER", "ADMIN", "BILLING_OFFICER", "ACCOUNTS_OFFICER")

				.requestMatchers(HttpMethod.POST, "/payments/initiate").hasRole("USER")
				.requestMatchers(HttpMethod.POST, "/payments/confirm").hasRole("USER")
				.requestMatchers(HttpMethod.POST, "/payments/offline").hasRole("ACCOUNTS_OFFICER")
				.requestMatchers(HttpMethod.GET, "/payments/**")
				.hasAnyRole("USER", "ADMIN", "BILLING_OFFICER", "ACCOUNTS_OFFICER")

				.requestMatchers(HttpMethod.GET, "/meters/**").authenticated()

				.anyRequest().authenticated()).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

}
