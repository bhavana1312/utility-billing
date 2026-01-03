package com.utilitybilling.consumerservice.dto;

import java.time.Instant;

import lombok.*;

@Data
@Builder
public class ConsumerResponse{
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private Instant createdAt;
    private boolean active;
}
