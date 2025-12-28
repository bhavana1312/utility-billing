package com.utilitybilling.authservice.service;

import com.utilitybilling.authservice.dto.*;
import com.utilitybilling.authservice.exception.*;
import com.utilitybilling.authservice.model.User;
import com.utilitybilling.authservice.repository.UserRepository;
import com.utilitybilling.authservice.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository repo;

	@Mock
	private BCryptPasswordEncoder encoder;

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private AuthService service;

	@Test
	void register_success() {
		RegisterRequest r = new RegisterRequest();
		r.setUsername("user");
		r.setEmail("u@mail.com");
		r.setPassword("pass");
		r.setRoles(List.of("ADMIN"));

		when(repo.existsByUsername("user")).thenReturn(false);
		when(repo.existsByEmail("u@mail.com")).thenReturn(false);
		when(encoder.encode("pass")).thenReturn("hashed");

		service.register(r);

		verify(repo, times(1)).save(any(User.class));
	}

	@Test
	void register_duplicateUsername() {
		RegisterRequest r = new RegisterRequest();
		r.setUsername("user");

		when(repo.existsByUsername("user")).thenReturn(true);

		assertThrows(UserAlreadyExistsException.class, () -> service.register(r));
	}

	@Test
	void login_success() {
		User u = User.builder().username("user").password("hashed").roles(List.of("ADMIN")).build();

		LoginRequest req = new LoginRequest();
		req.setUsername("user");
		req.setPassword("pass");

		when(repo.findByUsername("user")).thenReturn(Optional.of(u));
		when(encoder.matches("pass", "hashed")).thenReturn(true);
		when(jwtUtil.generateToken("user", List.of("ADMIN"))).thenReturn("jwt");

		LoginResponse res = service.login(req);

		assertEquals("jwt", res.getToken());
	}

	@Test
	void login_wrongPassword() {
		User u = User.builder().password("hashed").build();

		LoginRequest req = new LoginRequest();
		req.setUsername("user");
		req.setPassword("wrong");

		when(repo.findByUsername("user")).thenReturn(Optional.of(u));
		when(encoder.matches("wrong", "hashed")).thenReturn(false);

		assertThrows(InvalidCredentialsException.class, () -> service.login(req));
	}

	@Test
	void changePassword_success() {
		User u = User.builder().password("old").build();

		ChangePasswordRequest req = new ChangePasswordRequest();
		req.setUsername("user");
		req.setOldPassword("old");
		req.setNewPassword("new");

		when(repo.findByUsername("user")).thenReturn(Optional.of(u));
		when(encoder.matches("old", "old")).thenReturn(true);
		when(encoder.matches("new", "old")).thenReturn(false);
		when(encoder.encode("new")).thenReturn("newHashed");

		service.changePassword("user", req);

		verify(repo).save(any(User.class));
	}

	@Test
	void forgotPassword_success() {
		User u = new User();
		ForgotPasswordRequest req = new ForgotPasswordRequest();
		req.setEmail("u@mail.com");
		when(repo.findByEmail("u@mail.com")).thenReturn(Optional.of(u));

		service.forgotPassword(req);

		verify(repo).save(u);
		assertNotNull(u.getResetToken());
	}

	@Test
	void resetPassword_success() {
		User u = User.builder().resetToken("token").resetTokenExpiry(Instant.now().plusSeconds(100)).build();

		ResetPasswordRequest req = new ResetPasswordRequest();
		req.setResetToken("token");
		req.setNewPassword("new");

		when(repo.findByResetToken("token")).thenReturn(Optional.of(u));
		when(encoder.encode("new")).thenReturn("hashed");

		service.resetPassword(req);

		verify(repo).save(u);
		assertNull(u.getResetToken());
	}
	
	@Test
	void register_duplicateEmail(){
	    RegisterRequest r=new RegisterRequest();
	    r.setUsername("user");
	    r.setEmail("u@mail.com");

	    when(repo.existsByUsername("user")).thenReturn(false);
	    when(repo.existsByEmail("u@mail.com")).thenReturn(true);

	    assertThrows(UserAlreadyExistsException.class,()->service.register(r));
	}

	@Test
	void register_defaultRoleAssigned(){
	    RegisterRequest r=new RegisterRequest();
	    r.setUsername("user");
	    r.setEmail("u@mail.com");
	    r.setPassword("pass");
	    r.setRoles(null);

	    when(repo.existsByUsername("user")).thenReturn(false);
	    when(repo.existsByEmail("u@mail.com")).thenReturn(false);
	    when(encoder.encode("pass")).thenReturn("hashed");

	    service.register(r);

	    ArgumentCaptor<User> captor=ArgumentCaptor.forClass(User.class);
	    verify(repo).save(captor.capture());

	    assertEquals(List.of("ROLE_USER"),captor.getValue().getRoles());
	}

	@Test
	void login_userNotFound(){
		LoginRequest req = new LoginRequest();
		req.setUsername("user");
		req.setPassword("p");
		
	    when(repo.findByUsername("user")).thenReturn(Optional.empty());
	    assertThrows(UserNotFoundException.class,
	            ()->service.login(req));
	}

	@Test
	void changePassword_userNotFound(){
	    when(repo.findByUsername("user")).thenReturn(Optional.empty());
	    assertThrows(UserNotFoundException.class,
	            ()->service.changePassword("user",new ChangePasswordRequest()));
	}

	@Test
	void changePassword_oldPasswordWrong(){
	    User u=User.builder().password("old").build();

	    when(repo.findByUsername("user")).thenReturn(Optional.of(u));
	    when(encoder.matches("wrong","old")).thenReturn(false);

	    ChangePasswordRequest r=new ChangePasswordRequest();
	    r.setOldPassword("wrong");
	    r.setNewPassword("new");

	    assertThrows(InvalidCredentialsException.class,
	            ()->service.changePassword("user",r));
	}

	@Test
	void changePassword_sameAsOld(){
	    User u=User.builder().password("old").build();

	    when(repo.findByUsername("user")).thenReturn(Optional.of(u));
	    when(encoder.matches("old","old")).thenReturn(true);
	    when(encoder.matches("old","old")).thenReturn(true);

	    ChangePasswordRequest r=new ChangePasswordRequest();
	    r.setOldPassword("old");
	    r.setNewPassword("old");

	    assertThrows(InvalidCredentialsException.class,
	            ()->service.changePassword("user",r));
	}

	@Test
	void forgotPassword_userNotFound(){
		ForgotPasswordRequest req = new ForgotPasswordRequest();
		req.setEmail("x@mail.com");
	    when(repo.findByEmail("x@mail.com")).thenReturn(Optional.empty());
	    assertThrows(UserNotFoundException.class,
	            ()->service.forgotPassword(req));
	}

	@Test
	void resetPassword_invalidToken(){
		ResetPasswordRequest req = new ResetPasswordRequest();
		req.setResetToken("bad");
		req.setNewPassword("new");
	    when(repo.findByResetToken("bad")).thenReturn(Optional.empty());
	    assertThrows(InvalidTokenException.class,
	            ()->service.resetPassword(req));
	}

	@Test
	void resetPassword_expiredToken(){
	    User u=User.builder()
	            .resetToken("token")
	            .resetTokenExpiry(Instant.now().minusSeconds(10))
	            .build();
	    ResetPasswordRequest req = new ResetPasswordRequest();
		req.setResetToken("token");
		req.setNewPassword("new");

	    when(repo.findByResetToken("token")).thenReturn(Optional.of(u));

	    assertThrows(InvalidTokenException.class,
	            ()->service.resetPassword(req));
	}

}
