package com.utilitybilling.consumerservice.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utilitybilling.consumerservice.dto.ConsumerResponse;
import com.utilitybilling.consumerservice.dto.ExistsResponse;
import com.utilitybilling.consumerservice.dto.UpdateConsumerRequest;
import com.utilitybilling.consumerservice.service.ConsumerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
	public ResponseEntity<Page<ConsumerResponse>> getAll(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {
		return ResponseEntity.ok(service.getAll(page, size));
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

	@GetMapping("/username/{username}")
	public ResponseEntity<ConsumerResponse> getByUsername(@PathVariable("username") String username) {
		return ResponseEntity.ok(service.getByUsername(username));
	}

}
