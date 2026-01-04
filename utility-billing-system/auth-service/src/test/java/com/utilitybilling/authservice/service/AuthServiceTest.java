package com.utilitybilling.authservice.service;

import com.utilitybilling.authservice.dto.*;
import com.utilitybilling.authservice.exception.*;
import com.utilitybilling.authservice.feign.NotificationClient;
import com.utilitybilling.authservice.model.User;
import com.utilitybilling.authservice.repository.UserRepository;
import com.utilitybilling.authservice.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@InjectMocks
	private AuthService service;

	@Mock
	private UserRepository repo;
	@Mock
	private BCryptPasswordEncoder encoder;
	@Mock
	private JwtUtil jwtUtil;
	@Mock
	private NotificationClient notificationClient;

	@Test
	void register_success_default_role() {
		RegisterRequest r = new RegisterRequest();
		r.setUsername("u");
		r.setEmail("e");
		r.setPassword("p");

		when(repo.existsByUsername("u")).thenReturn(false);
		when(repo.existsByEmail("e")).thenReturn(false);
		when(encoder.encode("p")).thenReturn("enc");

		service.register(r);

		verify(repo).save(any(User.class));
	}

	@Test
	void register_username_exists() {
		RegisterRequest r = new RegisterRequest();
		r.setUsername("u");

		when(repo.existsByUsername("u")).thenReturn(true);

		assertThrows(UserAlreadyExistsException.class, () -> service.register(r));
	}

	@Test
	void register_email_exists() {
		RegisterRequest r = new RegisterRequest();
		r.setUsername("u");
		r.setEmail("e");

		when(repo.existsByUsername("u")).thenReturn(false);
		when(repo.existsByEmail("e")).thenReturn(true);

		assertThrows(UserAlreadyExistsException.class, () -> service.register(r));
	}

	@Test
	void register_with_roles_provided() {
		RegisterRequest r = new RegisterRequest();
		r.setUsername("u");
		r.setEmail("e");
		r.setPassword("p");
		r.setRoles(List.of("ROLE_ADMIN"));

		when(repo.existsByUsername("u")).thenReturn(false);
		when(repo.existsByEmail("e")).thenReturn(false);
		when(encoder.encode("p")).thenReturn("enc");

		service.register(r);

		verify(repo).save(any(User.class));
	}

	@Test
	void login_success() {
		User u = User.builder().username("u").password("enc").roles(List.of("ROLE_USER")).build();

		when(repo.findByUsername("u")).thenReturn(Optional.of(u));
		when(encoder.matches("p", "enc")).thenReturn(true);
		when(jwtUtil.generateToken("u", u.getRoles())).thenReturn("jwt");

		LoginRequest req = new LoginRequest();
		req.setUsername("u");
		req.setPassword("p");

		LoginResponse res = service.login(req);

		assertEquals("jwt", res.getToken());
	}

	@Test
	void login_user_not_found() {
		when(repo.findByUsername("u")).thenReturn(Optional.empty());
		LoginRequest req = new LoginRequest();
		req.setUsername("u");
		req.setPassword("p");
		assertThrows(UserNotFoundException.class, () -> service.login(req));
	}

	@Test
	void login_invalid_password() {
		User u = User.builder().username("u").password("enc").build();

		when(repo.findByUsername("u")).thenReturn(Optional.of(u));
		when(encoder.matches("p", "enc")).thenReturn(false);
		LoginRequest req = new LoginRequest();
		req.setUsername("u");
		req.setPassword("p");

		assertThrows(InvalidCredentialsException.class, () -> service.login(req));
	}

	@Test
	void changePassword_success() {
		User u = User.builder().password("oldEnc").email("e").build();

		when(repo.findByUsername("u")).thenReturn(Optional.of(u));
		when(encoder.matches("old", "oldEnc")).thenReturn(true);
		when(encoder.matches("new", "oldEnc")).thenReturn(false);
		when(encoder.encode("new")).thenReturn("newEnc");

		ChangePasswordRequest cpr = new ChangePasswordRequest();
		cpr.setUsername("u");
		cpr.setOldPassword("old");
		cpr.setNewPassword("new");

		service.changePassword("u", cpr);

		verify(repo).save(u);
		verify(notificationClient).send(any());
	}

	@Test
	void changePassword_user_not_found() {
		when(repo.findByUsername("u")).thenReturn(Optional.empty());
		ChangePasswordRequest cpr = new ChangePasswordRequest();
		assertThrows(UserNotFoundException.class, () -> service.changePassword("u", cpr));
	}

	@Test
	void changePassword_wrong_old_password() {
		User u = User.builder().password("enc").build();

		when(repo.findByUsername("u")).thenReturn(Optional.of(u));
		when(encoder.matches(any(), any())).thenReturn(false);
		ChangePasswordRequest cpr = new ChangePasswordRequest();

		assertThrows(InvalidCredentialsException.class, () -> service.changePassword("u", cpr));
	}

	@Test
	void changePassword_same_new_password() {
		User u = User.builder().password("enc").build();

		when(repo.findByUsername("u")).thenReturn(Optional.of(u));
		when(encoder.matches(any(), any())).thenReturn(true);

		ChangePasswordRequest cpr = new ChangePasswordRequest();

		assertThrows(InvalidCredentialsException.class, () -> service.changePassword("u", cpr));
	}

	@Test
	void forgotPassword_success() {
		User u = User.builder().email("e").build();

		when(repo.findByEmail("e")).thenReturn(Optional.of(u));

		ForgotPasswordRequest fpr = new ForgotPasswordRequest();
		fpr.setEmail("e");

		service.forgotPassword(fpr);

		verify(repo).save(u);
		verify(notificationClient).send(any());
	}

	@Test
	void forgotPassword_user_not_found() {
		when(repo.findByEmail("e")).thenReturn(Optional.empty());

		ForgotPasswordRequest fpr = new ForgotPasswordRequest();
		fpr.setEmail("e");
		assertThrows(UserNotFoundException.class, () -> service.forgotPassword(fpr));
	}

	@Test
	void resetPassword_success() {
		User u = User.builder().resetToken("t").resetTokenExpiry(Instant.now().plusSeconds(60)).email("e").build();

		when(repo.findByResetToken("t")).thenReturn(Optional.of(u));
		when(encoder.encode("n")).thenReturn("enc");

		ResetPasswordRequest rpr = new ResetPasswordRequest();
		rpr.setResetToken("t");
		rpr.setNewPassword("n");
		service.resetPassword(rpr);

		verify(repo).save(u);
		verify(notificationClient).send(any());
	}

	@Test
	void resetPassword_invalid_token_lambda() {
		when(repo.findByResetToken("t")).thenReturn(Optional.empty());

		ResetPasswordRequest rpr = new ResetPasswordRequest();
		rpr.setResetToken("t");
		rpr.setNewPassword("n");

		assertThrows(InvalidTokenException.class, () -> service.resetPassword(rpr));
	}

	@Test
	void resetPassword_expired_token() {
		User u = User.builder().resetToken("t").resetTokenExpiry(Instant.now().minusSeconds(10)).build();

		when(repo.findByResetToken("t")).thenReturn(Optional.of(u));

		ResetPasswordRequest rpr = new ResetPasswordRequest();
		rpr.setResetToken("t");
		rpr.setNewPassword("n");

		assertThrows(InvalidTokenException.class, () -> service.resetPassword(rpr));
	}
}
