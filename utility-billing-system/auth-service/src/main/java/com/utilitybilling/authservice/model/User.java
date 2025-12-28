package com.utilitybilling.authservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection="users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;
    private String username;
    private String email;
    private String password;
    private List<String> roles;
    private boolean enabled;
    private String resetToken;
    private Instant resetTokenExpiry;
    private Instant passwordUpdatedAt;
    private Instant createdAt;
}
