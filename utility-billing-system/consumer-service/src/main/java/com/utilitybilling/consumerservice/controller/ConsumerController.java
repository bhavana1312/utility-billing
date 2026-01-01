package com.utilitybilling.consumerservice.controller;

import com.utilitybilling.consumerservice.dto.*;
import com.utilitybilling.consumerservice.service.ConsumerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consumers")
@RequiredArgsConstructor
public class ConsumerController {

	private final ConsumerService service;

	@PostMapping("/from-request/{id}")
	public ResponseEntity<ConsumerResponse> approve(@PathVariable("id") String id) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.approve(id));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ConsumerResponse> get(@PathVariable("id") String id) {
		return ResponseEntity.ok(service.getById(id));
	}

	@GetMapping
	public ResponseEntity<List<ConsumerResponse>> getAll() {
		return ResponseEntity.ok(service.getAll());
	}

	@PutMapping("/{id}")
	public ResponseEntity<ConsumerResponse> update(@PathVariable("id") String id,
			@Valid @RequestBody UpdateConsumerRequest r) {
		return ResponseEntity.ok(service.update(id, r));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deactivate(@PathVariable("id") String id) {
		service.deactivate(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/exists")
	public ResponseEntity<ExistsResponse> exists(@PathVariable("id") String id) {
		return ResponseEntity.ok(new ExistsResponse(service.exists(id)));
	}
}
