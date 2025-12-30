package com.utilitybilling.meterservice.dto;

import com.utilitybilling.meterservice.model.UtilityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateConnectionRequest{

    @NotBlank(message="Consumer ID is required")
    private String consumerId;
    
    @NotBlank(message="Email is required")
    private String email;

    @NotNull(message="Utility type is required")
    private UtilityType utilityType;

    @NotBlank(message="Address is required")
    private String address;
}
