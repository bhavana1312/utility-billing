package com.utilitybilling.tariffservice.dto;

import com.utilitybilling.tariffservice.model.OverduePenaltySlab;
import com.utilitybilling.tariffservice.model.TariffSlab;
import com.utilitybilling.tariffservice.model.UtilityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateTariffRequest {

	@NotNull
	private UtilityType utilityType;

	@NotEmpty
	private List<@Valid TariffSlab> slabs;

	@Min(0)
	private double fixedCharge;

	@Min(0)
	@Max(100)
	private double taxPercentage;
	
	@NotEmpty
	private List<OverduePenaltySlab> overduePenaltySlabs;

	@NotNull
	private LocalDate effectiveFrom;
}
