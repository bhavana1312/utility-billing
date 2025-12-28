package com.utilitybilling.authservice.model;

import jakarta.validation.constraints.*;
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
public class User{

    @Id
    private String id;

    @NotBlank(message="Username is required")
    @Size(min=3,max=50,message="Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message="Email is required")
    @Email(message="Invalid email format")
    private String email;

    @NotBlank(message="Password is required")
    @Size(min=8,message="Password must be at least 8 characters")
    private String password;

    @NotEmpty(message="At least one role is required")
    private List<@NotBlank String> roles;

    private boolean enabled;

    private String resetToken;

    private Instant resetTokenExpiry;

    private Instant passwordUpdatedAt;

    @NotNull
    private Instant createdAt;
}
