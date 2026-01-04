package com.utilitybilling.authservice.service;

import com.utilitybilling.authservice.dto.*;
import com.utilitybilling.authservice.exception.*;
import com.utilitybilling.authservice.feign.NotificationClient;
import com.utilitybilling.authservice.feign.NotificationRequest;
import com.utilitybilling.authservice.model.User;
import com.utilitybilling.authservice.repository.UserRepository;
import com.utilitybilling.authservice.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository repo;
	private final BCryptPasswordEncoder encoder;
	private final JwtUtil jwtUtil;
	private final NotificationClient notificationClient;

	public void register(RegisterRequest r) {
		if (repo.existsByUsername(r.getUsername()))
			throw new UserAlreadyExistsException("User already exists");
		if (repo.existsByEmail(r.getEmail()))
			throw new UserAlreadyExistsException("Email already exists");

		var roles = r.getRoles();
		if (roles == null || roles.isEmpty()) {
			roles = List.of("ROLE_USER");
		}

		repo.save(User.builder().username(r.getUsername()).email(r.getEmail()).password(encoder.encode(r.getPassword()))
				.roles(roles).enabled(true).createdAt(Instant.now()).passwordUpdatedAt(Instant.now()).build());
	}

	public LoginResponse login(LoginRequest r) {
		User u = repo.findByUsername(r.getUsername()).orElseThrow(UserNotFoundException::new);

		if (!encoder.matches(r.getPassword(), u.getPassword()))
			throw new InvalidCredentialsException("Invalid credentials");

		return new LoginResponse(jwtUtil.generateToken(u.getUsername(), u.getRoles()));
	}

	public void changePassword(String username, ChangePasswordRequest r) {
		User u = repo.findByUsername(username).orElseThrow(UserNotFoundException::new);

		if (!encoder.matches(r.getOldPassword(), u.getPassword()))
			throw new InvalidCredentialsException("Wrong password");

		if (encoder.matches(r.getNewPassword(), u.getPassword()))
			throw new InvalidCredentialsException("New password must be different");

		u.setPassword(encoder.encode(r.getNewPassword()));
		u.setPasswordUpdatedAt(Instant.now());
		repo.save(u);

		notificationClient.send(NotificationRequest.builder().email(u.getEmail()).type("PASSWORD_CHANGED")
				.subject("Password changed successfully")
				.message("Your password was changed successfully. If this wasn’t you, contact support immediately.")
				.build());
	}

	public void forgotPassword(ForgotPasswordRequest r) {
		User u = repo.findByEmail(r.getEmail()).orElseThrow(UserNotFoundException::new);

		String token = UUID.randomUUID().toString();

		u.setResetToken(token);
		u.setResetTokenExpiry(Instant.now().plusSeconds(900));
		repo.save(u);

		String resetLink = "http://localhost:4200/reset-password?token=" + token;

		notificationClient.send(NotificationRequest.builder().email(u.getEmail()).type("PASSWORD_RESET")
				.subject("Reset your password").message("Click the link below to reset your password:\n\n" + resetLink
						+ "\n\nThis link expires in 15 minutes.")
				.build());
	}

	public void resetPassword(ResetPasswordRequest r) {
		User u = repo.findByResetToken(r.getResetToken())
				.orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

		if (u.getResetTokenExpiry().isBefore(Instant.now()))
			throw new InvalidTokenException("Reset token expired");

		u.setPassword(encoder.encode(r.getNewPassword()));
		u.setPasswordUpdatedAt(Instant.now());
		u.setResetToken(null);
		u.setResetTokenExpiry(null);
		repo.save(u);

		notificationClient.send(NotificationRequest.builder().email(u.getEmail()).type("PASSWORD_CHANGED")
				.subject("Your password was reset successfully")
				.message(
						"Your password has been reset successfully.\n\n If you did not perform this action, please contact support immediately.")
				.build());
	}
}
