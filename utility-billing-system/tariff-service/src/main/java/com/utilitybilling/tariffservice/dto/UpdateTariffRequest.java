package com.utilitybilling.tariffservice.dto;

import com.utilitybilling.tariffservice.model.OverduePenaltySlab;
import com.utilitybilling.tariffservice.model.TariffSlab;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateTariffRequest {

    @NotEmpty(message = "Tariff slabs list must not be empty")
    private List<@Valid TariffSlab> slabs;

    @Min(value = 0, message = "Fixed charge must be zero or a positive value")
    private double fixedCharge;

    @Min(value = 0, message = "Tax percentage must be at least 0")
    @Max(value = 100, message = "Tax percentage must not exceed 100")
    private double taxPercentage;
    
    private List<OverduePenaltySlab> overduePenaltySlabs;

    @NotNull(message = "Effective from date must be provided")
    private LocalDate effectiveFrom;
}
