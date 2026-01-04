package com.utilitybilling.authservice.service;

import com.utilitybilling.authservice.dto.InternalCreateUserRequest;
import com.utilitybilling.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class InternalUserServiceTest {

    @Mock
    UserRepository repository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    InternalUserService service;

    @Test
    void createUser_success() {
        InternalCreateUserRequest r = new InternalCreateUserRequest();
        r.setUsername("user");
        r.setEmail("mail@test.com");
        r.setPassword("pass");

        when(repository.findByEmail("mail@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");

        service.createUser(r);

        verify(repository).save(any());
    }

    @Test
    void createUser_alreadyExists() {
        InternalCreateUserRequest r = new InternalCreateUserRequest();
        r.setEmail("mail@test.com");

        when(repository.findByEmail("mail@test.com")).thenReturn(Optional.of(mock()));

        assertThrows(IllegalStateException.class, () -> service.createUser(r));
    }
}
