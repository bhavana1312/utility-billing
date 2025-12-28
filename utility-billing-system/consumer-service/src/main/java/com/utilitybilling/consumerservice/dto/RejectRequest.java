package com.utilitybilling.consumerservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectRequest{
    @NotBlank(message="Rejection reason required")
    private String reason;
}
