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

				Tariff.builder().utilityType(UtilityType.ELECTRICITY).plans(List.of(

						TariffPlanConfig.builder().plan(TariffPlan.DOMESTIC).active(true).fixedCharge(75)
								.taxPercentage(5).effectiveFrom(LocalDate.now())
								.slabs(List.of(new TariffSlab(1, 100, 3.5), new TariffSlab(101, 200, 5.0),
										new TariffSlab(201, 500, 6.5), new TariffSlab(501, Integer.MAX_VALUE, 8.0)))
								.overduePenaltySlabs(penalties).build(),

						TariffPlanConfig.builder().plan(TariffPlan.COMMERCIAL).active(true).fixedCharge(200)
								.taxPercentage(12).effectiveFrom(LocalDate.now())
								.slabs(List.of(new TariffSlab(1, 200, 6.5), new TariffSlab(201, 500, 8.0),
										new TariffSlab(501, Integer.MAX_VALUE, 10.0)))
								.overduePenaltySlabs(penalties).build(),

						TariffPlanConfig.builder().plan(TariffPlan.INDUSTRIAL).active(true).fixedCharge(500)
								.taxPercentage(18).effectiveFrom(LocalDate.now())
								.slabs(List.of(new TariffSlab(1, 500, 7.5), new TariffSlab(501, 1000, 9.0),
										new TariffSlab(1001, Integer.MAX_VALUE, 11.5)))
								.overduePenaltySlabs(penalties).build()))
						.build(),

				Tariff.builder().utilityType(UtilityType.WATER).plans(List.of(

						TariffPlanConfig.builder().plan(TariffPlan.DOMESTIC).active(true).fixedCharge(50)
								.taxPercentage(3).effectiveFrom(LocalDate.now())
								.slabs(List.of(new TariffSlab(1, 30, 2.0), new TariffSlab(31, 60, 3.5),
										new TariffSlab(61, Integer.MAX_VALUE, 5.0)))
								.overduePenaltySlabs(penalties).build(),

						TariffPlanConfig.builder().plan(TariffPlan.COMMERCIAL).active(true).fixedCharge(150)
								.taxPercentage(8).effectiveFrom(LocalDate.now())
								.slabs(List.of(new TariffSlab(1, 50, 4.5), new TariffSlab(51, 150, 6.0),
										new TariffSlab(151, Integer.MAX_VALUE, 8.0)))
								.overduePenaltySlabs(penalties).build(),

						TariffPlanConfig.builder().plan(TariffPlan.INDUSTRIAL).active(true).fixedCharge(300)
								.taxPercentage(12).effectiveFrom(LocalDate.now())
								.slabs(List.of(new TariffSlab(1, 100, 6.0), new TariffSlab(101, 300, 8.5),
										new TariffSlab(301, Integer.MAX_VALUE, 11.0)))
								.overduePenaltySlabs(penalties).build()))
						.build(),

				Tariff.builder().utilityType(UtilityType.GAS).plans(List.of(

						TariffPlanConfig.builder().plan(TariffPlan.DOMESTIC).active(true).fixedCharge(60)
								.taxPercentage(5).effectiveFrom(LocalDate.now())
								.slabs(List.of(new TariffSlab(1, 20, 4.0), new TariffSlab(21, 50, 5.5),
										new TariffSlab(51, Integer.MAX_VALUE, 7.0)))
								.overduePenaltySlabs(penalties).build(),

						TariffPlanConfig.builder().plan(TariffPlan.COMMERCIAL).active(true).fixedCharge(180)
								.taxPercentage(10).effectiveFrom(LocalDate.now())
								.slabs(List.of(new TariffSlab(1, 50, 7.0), new TariffSlab(51, 150, 9.0),
										new TariffSlab(151, Integer.MAX_VALUE, 12.0)))
								.overduePenaltySlabs(penalties).build(),

						TariffPlanConfig.builder().plan(TariffPlan.INDUSTRIAL).active(true).fixedCharge(400)
								.taxPercentage(15).effectiveFrom(LocalDate.now())
								.slabs(List.of(new TariffSlab(1, 100, 9.0), new TariffSlab(101, 300, 12.0),
										new TariffSlab(301, Integer.MAX_VALUE, 15.0)))
								.overduePenaltySlabs(penalties).build()))
						.build()));
	}
}
