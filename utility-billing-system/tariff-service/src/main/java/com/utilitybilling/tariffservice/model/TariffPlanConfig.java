package com.utilitybilling.tariffservice.model;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffPlanConfig{

	private TariffPlan plan;

	private boolean active;

	private double fixedCharge;

	private double taxPercentage;

	private List<TariffSlab> slabs;

	private List<OverduePenaltySlab> overduePenaltySlabs;

	private LocalDate effectiveFrom;
}
