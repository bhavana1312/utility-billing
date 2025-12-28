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

    public GatewaySecurityConfig(JwtAuthenticationFilter jwtFilter){
        this.jwtFilter=jwtFilter;
    }

    // ✅ 1️⃣ ACTUATOR SECURITY CHAIN (HIGHEST PRIORITY)
    @Bean
    @Order(0)
    public SecurityFilterChain actuatorSecurity(HttpSecurity http)throws Exception{
        return http
                .securityMatcher("/actuator/**")
                .csrf(csrf->csrf.disable())
                .authorizeHttpRequests(auth->auth.anyRequest().permitAll())
                .build();
    }

    // ✅ 2️⃣ API SECURITY CHAIN (JWT + RBAC)
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http)throws Exception{
        return http
                .csrf(csrf->csrf.disable())
                .authorizeHttpRequests(auth->auth

                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers(
                                HttpMethod.POST,"/utilities/tariffs/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,"/utilities/tariffs/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,"/utilities/tariffs/**"
                        ).hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
