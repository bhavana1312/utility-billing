package com.utilitybilling.meterservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApproveConnectionRequest{

    @NotBlank(message="Meter number is required")
    private String meterNumber;
}
