package com.utilitybilling.consumerservice.service;

import com.utilitybilling.consumerservice.client.AuthClient;
import com.utilitybilling.consumerservice.dto.*;
import com.utilitybilling.consumerservice.exception.NotFoundException;
import com.utilitybilling.consumerservice.model.Consumer;
import com.utilitybilling.consumerservice.model.ConsumerRequest;
import com.utilitybilling.consumerservice.repository.ConsumerRepository;
import com.utilitybilling.consumerservice.repository.ConsumerRequestRepository;
import com.utilitybilling.consumerservice.util.PasswordGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumerService {

	private final ConsumerRepository consumerRepo;
	private final ConsumerRequestRepository requestRepo;
	private final AuthClient authClient;

	public ConsumerResponse createFromRequest(String requestId) {

		ConsumerRequest r = requestRepo.findById(requestId)
				.orElseThrow(() -> new NotFoundException("Request not found"));

		if (!"PENDING".equals(r.getStatus()))
			throw new IllegalStateException("Request already processed");

		String password = PasswordGenerator.generate();

		Consumer c = Consumer.builder().fullName(r.getFullName()).email(r.getEmail()).phone(r.getPhone())
				.addressLine1(r.getAddressLine1()).city(r.getCity()).state(r.getState()).postalCode(r.getPostalCode())
				.active(true).createdAt(Instant.now()).updatedAt(Instant.now()).build();

		c = consumerRepo.save(c);

		authClient.createUser(new CreateAuthUserRequest(r.getFullName(), r.getEmail(), password, List.of("ROLE_USER")));

		r.setStatus("APPROVED");
		r.setUpdatedAt(Instant.now());
		requestRepo.save(r);

		return ConsumerResponse.builder().id(c.getId()).fullName(c.getFullName()).email(c.getEmail()).password(password)
				.phone(c.getPhone()).active(true).build();
	}

	public ConsumerResponse getById(String id) {
		return map(find(id));
	}

	public List<ConsumerResponse> getAll() {
		return consumerRepo.findAll().stream().map(this::map).toList();
	}

	public ConsumerResponse update(String id, UpdateConsumerRequest r) {
		Consumer c = find(id);

		if (!c.isActive())
			throw new IllegalStateException("Inactive consumer cannot be updated");

		c.setFullName(r.getFullName());
		c.setPhone(r.getPhone());
		c.setAddressLine1(r.getAddressLine1());
		c.setCity(r.getCity());
		c.setState(r.getState());
		c.setPostalCode(r.getPostalCode());
		c.setUpdatedAt(Instant.now());

		return map(consumerRepo.save(c));
	}

	public void deactivate(String id) {
		Consumer c = find(id);
		c.setActive(false);
		c.setUpdatedAt(Instant.now());
		consumerRepo.save(c);
	}

	public boolean exists(String id) {
		return consumerRepo.existsById(id);
	}

	private Consumer find(String id) {
		return consumerRepo.findById(id).orElseThrow(() -> new NotFoundException("Consumer not found"));
	}

	private ConsumerResponse map(Consumer c) {
		return ConsumerResponse.builder().id(c.getId()).fullName(c.getFullName()).email(c.getEmail())
				.phone(c.getPhone()).active(c.isActive()).build();
	}
}
