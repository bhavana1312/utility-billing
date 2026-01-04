package com.utilitybilling.tariffservice.service;

import com.utilitybilling.tariffservice.dto.*;
import com.utilitybilling.tariffservice.exception.*;
import com.utilitybilling.tariffservice.model.*;
import com.utilitybilling.tariffservice.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class TariffService {

	private final TariffRepository repo;
	private static final String TARIFF_NOT_FOUND = "Tariff not found";

	public void createPlan(CreateTariffPlanRequest r) {

		Tariff tariff = repo.findByUtilityType(r.getUtilityType())
				.orElse(Tariff.builder().utilityType(r.getUtilityType()).plans(new ArrayList<>()).build());

		tariff.getPlans().stream().filter(p -> p.getPlan() == r.getPlan() && p.isActive()).findFirst().ifPresent(p -> {
			throw new BusinessException("Active plan already exists");
		});

		tariff.getPlans()
				.add(TariffPlanConfig.builder().plan(r.getPlan()).slabs(r.getSlabs()).fixedCharge(r.getFixedCharge())
						.taxPercentage(r.getTaxPercentage()).overduePenaltySlabs(r.getOverduePenaltySlabs())
						.effectiveFrom(r.getEffectiveFrom()).active(true).build());

		repo.save(tariff);
	}

	public TariffResponse getActivePlan(UtilityType type, TariffPlan plan) {

		Tariff tariff = repo.findByUtilityType(type).orElseThrow(() -> new NotFoundException(TARIFF_NOT_FOUND));

		TariffPlanConfig p = tariff.getPlans().stream().filter(tp -> tp.getPlan() == plan && tp.isActive()).findFirst()
				.orElseThrow(() -> new NotFoundException("Active plan not found"));

		return map(type, p);
	}

	public void deactivatePlan(UtilityType type, TariffPlan plan) {

		Tariff tariff = repo.findByUtilityType(type).orElseThrow(() -> new NotFoundException(TARIFF_NOT_FOUND));

		TariffPlanConfig p = tariff.getPlans().stream().filter(tp -> tp.getPlan() == plan && tp.isActive()).findFirst()
				.orElseThrow(() -> new NotFoundException("Active plan not found"));

		p.setActive(false);
		repo.save(tariff);
	}

	public void updatePlan(UtilityType type, TariffPlan plan, UpdateTariffPlanRequest r) {

		Tariff tariff = repo.findByUtilityType(type).orElseThrow(() -> new NotFoundException(TARIFF_NOT_FOUND));

		TariffPlanConfig existing = tariff.getPlans().stream().filter(tp -> tp.getPlan() == plan && tp.isActive())
				.findFirst().orElseThrow(() -> new BusinessException("No active plan"));

		existing.setActive(false);

		tariff.getPlans()
				.add(TariffPlanConfig.builder().plan(plan).slabs(r.getSlabs()).fixedCharge(r.getFixedCharge())
						.taxPercentage(r.getTaxPercentage()).overduePenaltySlabs(r.getOverduePenaltySlabs())
						.effectiveFrom(r.getEffectiveFrom()).active(true).build());

		repo.save(tariff);
	}

	private TariffResponse map(UtilityType type, TariffPlanConfig p) {
		return TariffResponse.builder().utilityType(type).plan(p.getPlan()).active(p.isActive()).slabs(p.getSlabs())
				.fixedCharge(p.getFixedCharge()).taxPercentage(p.getTaxPercentage())
				.overduePenaltySlabs(p.getOverduePenaltySlabs()).effectiveFrom(p.getEffectiveFrom()).build();
	}
}
