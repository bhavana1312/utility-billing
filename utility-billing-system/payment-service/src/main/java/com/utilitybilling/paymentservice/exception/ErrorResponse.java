package com.utilitybilling.paymentservice.exception;

import lombok.Data;

import java.time.Instant;

@Data
public class ErrorResponse {
    private String message;
    private int status;
    private Instant timestamp=Instant.now();
}
