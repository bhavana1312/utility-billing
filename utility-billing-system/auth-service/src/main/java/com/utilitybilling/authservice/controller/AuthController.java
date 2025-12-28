package com.utilitybilling.authservice.controller;

import com.utilitybilling.authservice.dto.*;
import com.utilitybilling.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest r){
        service.register(r);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r){
        return service.login(r);
    }

    @PostMapping("/change-password")
    public void change(@Valid @RequestBody ChangePasswordRequest r){
        String user=SecurityContextHolder.getContext().getAuthentication().getName();
        service.changePassword(user,r);
    }

    @PostMapping("/forgot-password")
    public String forgot(@Valid @RequestBody ForgotPasswordRequest r){
        return service.forgotPassword(r);
    }

    @PostMapping("/reset-password")
    public void reset(@Valid @RequestBody ResetPasswordRequest r){
        service.resetPassword(r);
    }
}
