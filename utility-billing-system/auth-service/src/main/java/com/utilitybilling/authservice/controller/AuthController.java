package com.utilitybilling.authservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.utilitybilling.authservice.dto.ChangePasswordRequest;
import com.utilitybilling.authservice.dto.ForgotPasswordRequest;
import com.utilitybilling.authservice.dto.LoginRequest;
import com.utilitybilling.authservice.dto.LoginResponse;
import com.utilitybilling.authservice.dto.RegisterRequest;
import com.utilitybilling.authservice.dto.ResetPasswordRequest;
import com.utilitybilling.authservice.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
