package com.utilitybilling.authservice.config;

import com.utilitybilling.authservice.model.User;
import com.utilitybilling.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AccountsOfficerDataInitializer{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Bean
    public CommandLineRunner seedAuthUsers(){
        return args -> {

            String rawPassword="Admin@123";

            if(rawPassword==null||rawPassword.isBlank()){
                throw new IllegalStateException("ADMIN_INITIAL_PASSWORD environment variable is not set");
            }

            Instant now=Instant.now();

            if(!userRepository.existsByUsername("admin")){
                userRepository.save(
                        User.builder()
                                .username("admin")
                                .email("22071a66d9@vnrvjiet.in")
                                .password(encoder.encode(rawPassword))
                                .roles(List.of("ROLE_ADMIN"))
                                .enabled(true)
                                .createdAt(now)
                                .passwordUpdatedAt(now)
                                .build()
                );
            }

            if(!userRepository.existsByUsername("billing_officer")){
                userRepository.save(
                        User.builder()
                                .username("billing_officer")
                                .email("22071a66d9@vnrvjiet.in")
                                .password(encoder.encode(rawPassword))
                                .roles(List.of("ROLE_BILLING_OFFICER"))
                                .enabled(true)
                                .createdAt(now)
                                .passwordUpdatedAt(now)
                                .build()
                );
            }

            if(!userRepository.existsByUsername("accounts_officer")){
                userRepository.save(
                        User.builder()
                                .username("accounts_officer")
                                .email("22071a66d9@vnrvjiet.in")
                                .password(encoder.encode(rawPassword))
                                .roles(List.of("ROLE_ACCOUNTS_OFFICER"))
                                .enabled(true)
                                .createdAt(now)
                                .passwordUpdatedAt(now)
                                .build()
                );
            }
        };
    }
}
