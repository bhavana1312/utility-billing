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
public class AuthController{

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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest r){
//        String user=SecurityContextHolder.getContext().getAuthentication().getName();
//        service.changePassword(user,r);
    	service.changePassword(r.getUsername(), r);
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest r){
        service.forgotPassword(r);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest r){
        service.resetPassword(r);
    }
}
