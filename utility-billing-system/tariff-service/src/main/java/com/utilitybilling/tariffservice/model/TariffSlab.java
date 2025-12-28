package com.utilitybilling.tariffservice.model;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TariffSlab{

    @Min(value=0,message="From unit must be >= 0")
    private int fromUnit;

    @Min(value=1,message="To unit must be >= 1")
    private int toUnit;

    @Min(value=0,message="Rate must be positive")
    private double ratePerUnit;
}
