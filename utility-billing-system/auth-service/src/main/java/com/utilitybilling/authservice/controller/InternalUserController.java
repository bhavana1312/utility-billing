package com.utilitybilling.authservice.controller;

import com.utilitybilling.authservice.dto.InternalCreateUserRequest;
import com.utilitybilling.authservice.service.InternalUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController{

    private final InternalUserService service;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody InternalCreateUserRequest r){
        service.createUser(r);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
