package com.utilitybilling.meterservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectConnectionRequest{
    @NotBlank(message="Rejection reason is required")
    private String reason;
}
