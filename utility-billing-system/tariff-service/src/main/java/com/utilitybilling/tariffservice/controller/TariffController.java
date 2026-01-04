package com.utilitybilling.tariffservice.controller;

import com.utilitybilling.tariffservice.dto.*;
import com.utilitybilling.tariffservice.model.*;
import com.utilitybilling.tariffservice.service.TariffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/utilities/tariffs")
@RequiredArgsConstructor
public class TariffController {

	private final TariffService service;

	@PostMapping("/plans")
	public ResponseEntity<Void> createPlan(@Valid @RequestBody CreateTariffPlanRequest r) {
		service.createPlan(r);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("/{utilityType}/plans/{plan}")
	public ResponseEntity<TariffResponse> getActivePlan(@PathVariable("utilityType") UtilityType utilityType,
			@PathVariable("plan") TariffPlan plan) {
		return ResponseEntity.ok(service.getActivePlan(utilityType, plan));
	}

	@DeleteMapping("/{utilityType}/plans/{plan}")
	public ResponseEntity<Void> deactivatePlan(@PathVariable("utilityType") UtilityType utilityType,
			@PathVariable("plan") TariffPlan plan) {
		service.deactivatePlan(utilityType, plan);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{utilityType}/plans/{plan}")
	public ResponseEntity<Void> updatePlan(@PathVariable("utilityType") UtilityType utilityType,
			@PathVariable("plan") TariffPlan plan, @Valid @RequestBody UpdateTariffPlanRequest r) {
		service.updatePlan(utilityType, plan, r);
		return ResponseEntity.noContent().build();
	}
}
