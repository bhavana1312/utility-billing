package com.utilitybilling.authservice.service;

import com.utilitybilling.authservice.dto.*;
import com.utilitybilling.authservice.exception.*;
import com.utilitybilling.authservice.model.User;
import com.utilitybilling.authservice.repository.UserRepository;
import com.utilitybilling.authservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository repo;
	private final BCryptPasswordEncoder encoder;
	private final JwtUtil jwtUtil;

	public void register(RegisterRequest r) {
		if (repo.existsByUsername(r.getUsername()))
			throw new UserAlreadyExistsException("User already exists");

		repo.save(User.builder().username(r.getUsername()).password(encoder.encode(r.getPassword())).roles(r.getRoles())
				.enabled(true).createdAt(Instant.now()).passwordUpdatedAt(Instant.now()).build());
	}

	public LoginResponse login(LoginRequest r) {
		User u = repo.findByUsername(r.getUsername()).orElseThrow(() -> new UserNotFoundException());

		if (!encoder.matches(r.getPassword(), u.getPassword()))
			throw new InvalidCredentialsException("Invalid credentials");

		return new LoginResponse(jwtUtil.generateToken(u.getUsername(), u.getRoles()));
	}

	public void changePassword(String username, ChangePasswordRequest r) {
		User u = repo.findByUsername(username).orElseThrow(UserNotFoundException::new);
		if (!encoder.matches(r.getOldPassword(), u.getPassword()))
			throw new InvalidCredentialsException("Wrong password");
		u.setPassword(encoder.encode(r.getNewPassword()));
		u.setPasswordUpdatedAt(Instant.now());
		repo.save(u);
	}

	public String forgotPassword(ForgotPasswordRequest r) {
		User u = repo.findByUsername(r.getUsername()).orElseThrow(UserNotFoundException::new);
		String token = UUID.randomUUID().toString();
		u.setResetToken(token);
		u.setResetTokenExpiry(Instant.now().plusSeconds(900));
		repo.save(u);
		return token;
	}

	public void resetPassword(ResetPasswordRequest r) {
		User u = repo.findByResetToken(r.getResetToken()).orElseThrow(() -> new RuntimeException("Invalid token"));

		if (u.getResetTokenExpiry().isBefore(Instant.now()))
			throw new RuntimeException("Token expired");

		u.setPassword(encoder.encode(r.getNewPassword()));
		u.setResetToken(null);
		u.setResetTokenExpiry(null);
		u.setPasswordUpdatedAt(Instant.now());
		repo.save(u);
	}
}
