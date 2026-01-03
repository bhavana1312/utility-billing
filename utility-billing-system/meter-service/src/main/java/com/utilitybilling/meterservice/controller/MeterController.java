package com.utilitybilling.meterservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utilitybilling.meterservice.dto.CreateConnectionRequest;
import com.utilitybilling.meterservice.dto.CreateMeterReadingRequest;
import com.utilitybilling.meterservice.dto.MeterDetailsResponse;
import com.utilitybilling.meterservice.dto.MeterReadingResponse;
import com.utilitybilling.meterservice.dto.RejectConnectionRequest;
import com.utilitybilling.meterservice.model.ConnectionRequest;
import com.utilitybilling.meterservice.model.Meter;
import com.utilitybilling.meterservice.service.MeterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/meters")
@RequiredArgsConstructor
public class MeterController {

	private final MeterService service;

	@PostMapping("/connection-requests")
	public ResponseEntity<Void> request(@Valid @RequestBody CreateConnectionRequest r) {
		service.requestConnection(r);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("/connection-requests")
	public ResponseEntity<List<ConnectionRequest>> all() {
		return ResponseEntity.ok(service.getAllRequests());
	}

	@PostMapping("/connection-requests/{id}/approve")
	public ResponseEntity<Void> approve(@PathVariable("id") String id) {
		service.approve(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/connection-requests/{id}/reject")
	public ResponseEntity<Void> reject(@PathVariable("id") String id, @Valid @RequestBody RejectConnectionRequest r) {
		service.reject(id, r.getReason());
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{meterNumber}")
	public ResponseEntity<MeterDetailsResponse> getMeter(@PathVariable("meterNumber") String meterNumber) {
		return ResponseEntity.ok(service.getMeter(meterNumber));
	}

	@GetMapping("/all")
	public ResponseEntity<List<Meter>> countMeters() {
		return ResponseEntity.ok(service.getAllMeters());
	}

	@GetMapping("/consumer/{consumerId}")
	public ResponseEntity<List<Meter>> byConsumer(@PathVariable("consumerId") String consumerId) {
		return ResponseEntity.ok(service.getMetersByConsumer(consumerId));
	}

	@DeleteMapping("/{meterNumber}")
	public ResponseEntity<Void> deactivate(@PathVariable("meterNumber") String meterNumber) {
		service.deactivateMeter(meterNumber);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/readings")
	public ResponseEntity<MeterReadingResponse> add(@Valid @RequestBody CreateMeterReadingRequest r) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.addReading(r));
	}

	@GetMapping("/{meterNumber}/last-reading")
	public ResponseEntity<Double> last(@PathVariable("meterNumber") String meterNumber) {
		return ResponseEntity.ok(service.getLastReading(meterNumber));
	}



}
