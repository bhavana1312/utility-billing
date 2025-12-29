package com.utilitybilling.billingservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateBillRequest{

    @NotBlank(message="Meter number is required")
    private String meterNumber;
}
