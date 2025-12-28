package com.utilitybilling.authservice.service;

import com.utilitybilling.authservice.dto.InternalCreateUserRequest;
import com.utilitybilling.authservice.model.User;
import com.utilitybilling.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InternalUserService {

	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;

	public void createUser(InternalCreateUserRequest r) {

		repository.findByEmail(r.getEmail()).ifPresent(u -> {
			throw new IllegalStateException("User already exists");
		});

		User u = User.builder().username(r.getUsername()).email(r.getEmail())
				.password(passwordEncoder.encode(r.getPassword())).roles(r.getRoles()).enabled(true)
				.createdAt(Instant.now()).passwordUpdatedAt(Instant.now()).build();

		repository.save(u);
	}
}
