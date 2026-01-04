package com.utilitybilling.tariffservice.service;

import com.utilitybilling.tariffservice.dto.*;
import com.utilitybilling.tariffservice.exception.*;
import com.utilitybilling.tariffservice.model.*;
import com.utilitybilling.tariffservice.repository.TariffRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TariffServiceTest {

	@Mock
	TariffRepository repo;

	private TariffService service;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		service = new TariffService(repo);
	}

	@Test
	void createPlan_success_new_tariff() {
		when(repo.findByUtilityType(UtilityType.ELECTRICITY)).thenReturn(Optional.empty());

		CreateTariffPlanRequest r = new CreateTariffPlanRequest();
		r.setUtilityType(UtilityType.ELECTRICITY);
		r.setPlan(TariffPlan.DOMESTIC);
		r.setSlabs(List.of());
		r.setFixedCharge(50);
		r.setTaxPercentage(10);
		r.setOverduePenaltySlabs(List.of());
		r.setEffectiveFrom(LocalDate.now());

		service.createPlan(r);

		verify(repo).save(any(Tariff.class));
	}

	@Test
	void createPlan_duplicate_active_plan() {
		TariffPlanConfig cfg = TariffPlanConfig.builder().plan(TariffPlan.DOMESTIC).active(true).build();

		Tariff t = Tariff.builder().utilityType(UtilityType.ELECTRICITY).plans(new ArrayList<>(List.of(cfg))).build();

		when(repo.findByUtilityType(UtilityType.ELECTRICITY)).thenReturn(Optional.of(t));

		CreateTariffPlanRequest r = new CreateTariffPlanRequest();
		r.setUtilityType(UtilityType.ELECTRICITY);
		r.setPlan(TariffPlan.DOMESTIC);

		assertThrows(BusinessException.class, () -> service.createPlan(r));
	}

	@Test
	void getActivePlan_success() {
		TariffPlanConfig cfg = TariffPlanConfig.builder().plan(TariffPlan.DOMESTIC).active(true)
				.effectiveFrom(LocalDate.now()).build();

		Tariff t = Tariff.builder().utilityType(UtilityType.WATER).plans(List.of(cfg)).build();

		when(repo.findByUtilityType(UtilityType.WATER)).thenReturn(Optional.of(t));

		TariffResponse res = service.getActivePlan(UtilityType.WATER, TariffPlan.DOMESTIC);

		assertEquals(TariffPlan.DOMESTIC, res.getPlan());
	}

	@Test
	void getActivePlan_tariff_not_found() {
		when(repo.findByUtilityType(UtilityType.WATER)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> service.getActivePlan(UtilityType.WATER, TariffPlan.DOMESTIC));
	}

	@Test
	void getActivePlan_plan_not_found() {
		Tariff t = Tariff.builder().utilityType(UtilityType.WATER).plans(List.of()).build();

		when(repo.findByUtilityType(UtilityType.WATER)).thenReturn(Optional.of(t));

		assertThrows(NotFoundException.class, () -> service.getActivePlan(UtilityType.WATER, TariffPlan.DOMESTIC));
	}

	@Test
	void deactivatePlan_success() {
		TariffPlanConfig cfg = TariffPlanConfig.builder().plan(TariffPlan.DOMESTIC).active(true).build();

		Tariff t = Tariff.builder().utilityType(UtilityType.GAS).plans(new ArrayList<>(List.of(cfg))).build();

		when(repo.findByUtilityType(UtilityType.GAS)).thenReturn(Optional.of(t));

		service.deactivatePlan(UtilityType.GAS, TariffPlan.DOMESTIC);

		assertFalse(cfg.isActive());
		verify(repo).save(t);
	}

	@Test
	void updatePlan_success() {
		TariffPlanConfig existing = TariffPlanConfig.builder().plan(TariffPlan.DOMESTIC).active(true).build();

		Tariff t = Tariff.builder().utilityType(UtilityType.ELECTRICITY).plans(new ArrayList<>(List.of(existing)))
				.build();

		when(repo.findByUtilityType(UtilityType.ELECTRICITY)).thenReturn(Optional.of(t));

		UpdateTariffPlanRequest r = new UpdateTariffPlanRequest();
		r.setSlabs(List.of());
		r.setFixedCharge(100);
		r.setTaxPercentage(5);
		r.setOverduePenaltySlabs(List.of());
		r.setEffectiveFrom(LocalDate.now());

		service.updatePlan(UtilityType.ELECTRICITY, TariffPlan.DOMESTIC, r);

		assertFalse(existing.isActive());
		verify(repo).save(t);
	}

	@Test
	void createPlan_existing_inactive_plan_allowed() {
		TariffPlanConfig inactive = TariffPlanConfig.builder().plan(TariffPlan.DOMESTIC).active(false).build();

		Tariff t = Tariff.builder().utilityType(UtilityType.ELECTRICITY).plans(new ArrayList<>(List.of(inactive)))
				.build();

		when(repo.findByUtilityType(UtilityType.ELECTRICITY)).thenReturn(Optional.of(t));

		CreateTariffPlanRequest r = new CreateTariffPlanRequest();
		r.setUtilityType(UtilityType.ELECTRICITY);
		r.setPlan(TariffPlan.DOMESTIC);
		r.setSlabs(List.of());
		r.setFixedCharge(50);
		r.setTaxPercentage(5);
		r.setOverduePenaltySlabs(List.of());
		r.setEffectiveFrom(LocalDate.now());

		service.createPlan(r);

		verify(repo).save(t);
	}

	@Test
	void deactivatePlan_plan_already_inactive() {
		TariffPlanConfig inactive = TariffPlanConfig.builder().plan(TariffPlan.DOMESTIC).active(false).build();

		Tariff t = Tariff.builder().utilityType(UtilityType.GAS).plans(List.of(inactive)).build();

		when(repo.findByUtilityType(UtilityType.GAS)).thenReturn(Optional.of(t));

		assertThrows(NotFoundException.class, () -> service.deactivatePlan(UtilityType.GAS, TariffPlan.DOMESTIC));
	}

	@Test
	void updatePlan_no_active_plan() {
		TariffPlanConfig inactive = TariffPlanConfig.builder().plan(TariffPlan.DOMESTIC).active(false).build();

		Tariff t = Tariff.builder().utilityType(UtilityType.ELECTRICITY).plans(List.of(inactive)).build();

		when(repo.findByUtilityType(UtilityType.ELECTRICITY)).thenReturn(Optional.of(t));

		UpdateTariffPlanRequest r = new UpdateTariffPlanRequest();
		r.setSlabs(List.of());
		r.setFixedCharge(100);
		r.setTaxPercentage(10);
		r.setOverduePenaltySlabs(List.of());
		r.setEffectiveFrom(LocalDate.now());

		assertThrows(BusinessException.class,
				() -> service.updatePlan(UtilityType.ELECTRICITY, TariffPlan.DOMESTIC, r));
	}

	@Test
	void getActivePlan_plan_inactive() {
		TariffPlanConfig inactive = TariffPlanConfig.builder().plan(TariffPlan.DOMESTIC).active(false).build();

		Tariff t = Tariff.builder().utilityType(UtilityType.WATER).plans(List.of(inactive)).build();

		when(repo.findByUtilityType(UtilityType.WATER)).thenReturn(Optional.of(t));

		assertThrows(NotFoundException.class, () -> service.getActivePlan(UtilityType.WATER, TariffPlan.DOMESTIC));
	}

}
