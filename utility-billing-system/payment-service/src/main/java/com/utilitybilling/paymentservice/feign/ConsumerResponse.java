package com.utilitybilling.paymentservice.feign;

import lombok.*;

@Data
@Builder
public class ConsumerResponse{
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private boolean active;
}
