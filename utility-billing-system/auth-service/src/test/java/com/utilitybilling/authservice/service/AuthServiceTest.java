package com.utilitybilling.authservice.service;

import com.utilitybilling.authservice.dto.*;
import com.utilitybilling.authservice.exception.*;
import com.utilitybilling.authservice.feign.NotificationClient;
import com.utilitybilling.authservice.feign.NotificationRequest;
import com.utilitybilling.authservice.model.User;
import com.utilitybilling.authservice.repository.UserRepository;
import com.utilitybilling.authservice.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository repo;

    @Mock
    BCryptPasswordEncoder encoder;

    @Mock
    JwtUtil jwtUtil;

    @Mock
    NotificationClient notificationClient;

    @InjectMocks
    AuthService authService;

    @Test
    void register_success() {
        RegisterRequest r = new RegisterRequest();
        r.setUsername("user");
        r.setEmail("user@mail.com");
        r.setPassword("password");

        when(repo.existsByUsername("user")).thenReturn(false);
        when(repo.existsByEmail("user@mail.com")).thenReturn(false);
        when(encoder.encode("password")).thenReturn("hashed");

        authService.register(r);

        verify(repo).save(any(User.class));
    }

    @Test
    void register_usernameExists() {
        RegisterRequest r = new RegisterRequest();
        r.setUsername("user");

        when(repo.existsByUsername("user")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(r));
    }

    @Test
    void login_success() {
        User u = new User();
        u.setUsername("user");
        u.setPassword("hashed");
        u.setRoles(List.of("ROLE_USER"));

        LoginRequest r = new LoginRequest();
        r.setUsername("user");
        r.setPassword("password");

        when(repo.findByUsername("user")).thenReturn(Optional.of(u));
        when(encoder.matches("password", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("user", u.getRoles())).thenReturn("jwt");

        LoginResponse response = authService.login(r);

        assertEquals("jwt", response.getToken());
    }

    @Test
    void login_invalidPassword() {
        User u = new User();
        u.setPassword("hashed");

        LoginRequest r = new LoginRequest();
        r.setUsername("user");
        r.setPassword("bad");

        when(repo.findByUsername("user")).thenReturn(Optional.of(u));
        when(encoder.matches(any(), any())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(r));
    }

    @Test
    void changePassword_success() {
        User u = new User();
        u.setUsername("user");
        u.setEmail("mail@test.com");
        u.setPassword("old");

        ChangePasswordRequest r = new ChangePasswordRequest();
        r.setUsername("user");
        r.setOldPassword("oldPass");
        r.setNewPassword("newPass");

        when(repo.findByUsername("user")).thenReturn(Optional.of(u));
        when(encoder.matches("oldPass", "old")).thenReturn(true);
        when(encoder.matches("newPass", "old")).thenReturn(false);
        when(encoder.encode("newPass")).thenReturn("encoded");

        authService.changePassword("user", r);

        verify(repo).save(u);
        verify(notificationClient).send(any(NotificationRequest.class));
    }

    @Test
    void forgotPassword_success() {
        User u = new User();
        u.setEmail("mail@test.com");

        ForgotPasswordRequest r = new ForgotPasswordRequest();
        r.setEmail("mail@test.com");

        when(repo.findByEmail("mail@test.com")).thenReturn(Optional.of(u));

        authService.forgotPassword(r);

        assertNotNull(u.getResetToken());
        verify(notificationClient).send(any(NotificationRequest.class));
    }

    @Test
    void resetPassword_success() {
        User u = new User();
        u.setEmail("mail@test.com");
        u.setResetToken("token");
        u.setResetTokenExpiry(Instant.now().plusSeconds(300));

        ResetPasswordRequest r = new ResetPasswordRequest();
        r.setResetToken("token");
        r.setNewPassword("new");

        when(repo.findByResetToken("token")).thenReturn(Optional.of(u));
        when(encoder.encode("new")).thenReturn("encoded");

        authService.resetPassword(r);

        assertNull(u.getResetToken());
        verify(repo).save(u);
        verify(notificationClient).send(any(NotificationRequest.class));
    }
}
