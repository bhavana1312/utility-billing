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
public class AdminDataInitializer {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Bean
    public CommandLineRunner seedAdmin() {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {

                String rawPassword = System.getenv("ADMIN_INITIAL_PASSWORD");

                if (rawPassword == null || rawPassword.isBlank()) {
                    throw new IllegalStateException(
                        "ADMIN_INITIAL_PASSWORD environment variable is not set");
                }

                User admin = User.builder()
                        .username("admin")
                        .email("22071a66d9@vnrvjiet.in")
                        .password(encoder.encode(rawPassword))
                        .roles(List.of("ROLE_ADMIN"))
                        .enabled(true)
                        .createdAt(Instant.now())
                        .passwordUpdatedAt(Instant.now())
                        .build();

                userRepository.save(admin);
            }
        };
    }
}

