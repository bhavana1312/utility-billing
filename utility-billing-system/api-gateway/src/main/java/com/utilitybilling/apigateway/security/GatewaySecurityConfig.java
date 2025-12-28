package com.utilitybilling.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)throws Exception{
        return http
                .csrf(csrf->csrf.disable())
                .authorizeHttpRequests(auth->auth

                        .requestMatchers(
                                "/auth/login",
                                "/auth/register",
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/actuator/**"
                        ).permitAll()

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
