package com.utilitybilling.consumerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreateAuthUserRequest{
    private String username;
    private String email;
    private String password;
    private List<String> roles;
}
