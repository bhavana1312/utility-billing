package com.utilitybilling.meterservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateMeterReadingRequest{

    @NotBlank(message="Meter number is required")
    private String meterNumber;

    @Positive(message="Reading value must be positive")
    private double readingValue;
}
