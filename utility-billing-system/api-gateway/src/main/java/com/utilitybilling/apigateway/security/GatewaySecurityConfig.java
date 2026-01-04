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

	private static final String ROLE_ADMIN = "ADMIN";
	private static final String ROLE_BILLING_OFFICER = "BILLING_OFFICER";
	private static final String ROLE_ACCOUNTS_OFFICER = "ACCOUNTS_OFFICER";
	private static final String ROLE_USER = "USER";

	private static final String UTILITIES_TARIFFS = "/utilities/tariffs/**";
	private static final String CONSUMERS_ALL = "/consumers/**";

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

				.requestMatchers(HttpMethod.GET, UTILITIES_TARIFFS).permitAll()
				.requestMatchers(HttpMethod.POST, "/consumer-requests").permitAll()

				.requestMatchers(HttpMethod.POST, UTILITIES_TARIFFS).hasRole(ROLE_ADMIN)
				.requestMatchers(HttpMethod.PUT, UTILITIES_TARIFFS).hasRole(ROLE_ADMIN)
				.requestMatchers(HttpMethod.DELETE, UTILITIES_TARIFFS).hasRole(ROLE_ADMIN)

				.requestMatchers(HttpMethod.POST, "/consumers/from-request/**").hasRole(ROLE_ADMIN)
				.requestMatchers(HttpMethod.PUT, CONSUMERS_ALL).hasRole(ROLE_ADMIN)
				.requestMatchers(HttpMethod.DELETE, CONSUMERS_ALL).hasRole(ROLE_ADMIN)

				.requestMatchers(HttpMethod.GET, CONSUMERS_ALL)
				.hasAnyRole(ROLE_USER, ROLE_ADMIN, ROLE_BILLING_OFFICER, ROLE_ACCOUNTS_OFFICER)

				.requestMatchers(HttpMethod.GET, "/consumer-requests/**").hasRole(ROLE_ADMIN)
				.requestMatchers(HttpMethod.PUT, "/consumer-requests/**").hasRole(ROLE_ADMIN)

				.requestMatchers(HttpMethod.POST, "/meters/connection-requests").permitAll()
				.requestMatchers(HttpMethod.POST, "/meters/connection-requests/**").hasRole(ROLE_ADMIN)
				.requestMatchers(HttpMethod.GET, "meters/consumer/**")
				.hasAnyRole(ROLE_USER, ROLE_ADMIN, ROLE_BILLING_OFFICER, ROLE_ACCOUNTS_OFFICER)

				.requestMatchers(HttpMethod.POST, "/meters/readings").hasRole(ROLE_BILLING_OFFICER)

				.requestMatchers(HttpMethod.POST, "/billing/generate").hasRole(ROLE_BILLING_OFFICER)
				.requestMatchers(HttpMethod.GET, "/billing/**")
				.hasAnyRole(ROLE_USER, ROLE_ADMIN, ROLE_BILLING_OFFICER, ROLE_ACCOUNTS_OFFICER)

				.requestMatchers(HttpMethod.POST, "/payments/initiate").hasRole(ROLE_USER)
				.requestMatchers(HttpMethod.POST, "/payments/confirm").hasRole(ROLE_USER)
				.requestMatchers(HttpMethod.POST, "/payments/offline").hasRole(ROLE_ACCOUNTS_OFFICER)
				.requestMatchers(HttpMethod.GET, "/payments/**")
				.hasAnyRole(ROLE_USER, ROLE_ADMIN, ROLE_BILLING_OFFICER, ROLE_ACCOUNTS_OFFICER)

				.requestMatchers(HttpMethod.GET, "/meters/**").authenticated()

				.anyRequest().authenticated()).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

}
