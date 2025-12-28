package com.utilitybilling.tariffservice.dto;

import com.utilitybilling.tariffservice.model.TariffSlab;
import com.utilitybilling.tariffservice.model.UtilityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TariffResponse {
	private String id;
	private UtilityType utilityType;
	private List<TariffSlab> slabs;
	private double fixedCharge;
	private double taxPercentage;
	private boolean active;
	private LocalDate effectiveFrom;
}
