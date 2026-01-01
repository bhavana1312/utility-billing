package com.utilitybilling.tariffservice.dto;

import com.utilitybilling.tariffservice.model.*;
import com.utilitybilling.tariffservice.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class TariffSeedConfig implements CommandLineRunner {

	private final TariffRepository repo;

	@Override
	public void run(String... args) {

		if (repo.count() > 0)
			return;

		List<OverduePenaltySlab> penalties = List.of(new OverduePenaltySlab(1, 15, 2),
				new OverduePenaltySlab(16, 30, 5), new OverduePenaltySlab(31, Integer.MAX_VALUE, 10));

		repo.saveAll(List.of(

				Tariff.builder().utilityType(UtilityType.ELECTRICITY)
						.plans(List.of(plan(TariffPlan.DOMESTIC, 75, 5, penalties, new TariffSlab(1, 100, 3.5),
								new TariffSlab(101, 200, 5.0), new TariffSlab(201, 500, 6.5),
								new TariffSlab(501, Integer.MAX_VALUE, 8.0)),
								plan(TariffPlan.COMMERCIAL, 200, 12, penalties, new TariffSlab(1, 200, 6.5),
										new TariffSlab(201, 500, 8.0), new TariffSlab(501, Integer.MAX_VALUE, 10.0)),
								plan(TariffPlan.INDUSTRIAL, 500, 18, penalties, new TariffSlab(1, 500, 7.5),
										new TariffSlab(501, 1000, 9.0), new TariffSlab(1001, Integer.MAX_VALUE, 11.5))))
						.build(),

				Tariff.builder().utilityType(UtilityType.WATER).plans(List.of(
						plan(TariffPlan.DOMESTIC, 50, 3, penalties, new TariffSlab(1, 30, 2.0),
								new TariffSlab(31, 60, 3.5), new TariffSlab(61, Integer.MAX_VALUE, 5.0)),
						plan(TariffPlan.COMMERCIAL, 150, 8, penalties, new TariffSlab(1, 50, 4.5),
								new TariffSlab(51, 150, 6.0), new TariffSlab(151, Integer.MAX_VALUE, 8.0)),
						plan(TariffPlan.INDUSTRIAL, 300, 12, penalties, new TariffSlab(1, 100, 6.0),
								new TariffSlab(101, 300, 8.5), new TariffSlab(301, Integer.MAX_VALUE, 11.0))))
						.build(),

				Tariff.builder().utilityType(UtilityType.GAS)
						.plans(List.of(
								plan(TariffPlan.DOMESTIC, 60, 5, penalties, new TariffSlab(1, 20, 4.0),
										new TariffSlab(21, 50, 5.5), new TariffSlab(51, Integer.MAX_VALUE, 7.0)),
								plan(TariffPlan.COMMERCIAL, 180, 10, penalties, new TariffSlab(1, 50, 7.0),
										new TariffSlab(51, 150, 9.0), new TariffSlab(151, Integer.MAX_VALUE, 12.0)),
								plan(TariffPlan.INDUSTRIAL, 400, 15, penalties, new TariffSlab(1, 100, 9.0),
										new TariffSlab(101, 300, 12.0), new TariffSlab(301, Integer.MAX_VALUE, 15.0))))
						.build()));
	}

	private TariffPlanConfig plan(TariffPlan p, double fixed, double tax, List<OverduePenaltySlab> pen,
			TariffSlab... slabs) {
		return TariffPlanConfig.builder().plan(p).active(true).fixedCharge(fixed).taxPercentage(tax)
				.effectiveFrom(LocalDate.now()).slabs(List.of(slabs)).overduePenaltySlabs(pen).build();
	}
}
