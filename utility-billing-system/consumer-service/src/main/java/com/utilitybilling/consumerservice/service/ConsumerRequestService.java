package com.utilitybilling.consumerservice.service;

import com.utilitybilling.consumerservice.dto.CreateConsumerRequest;
import com.utilitybilling.consumerservice.dto.ConsumerRequestResponse;
import com.utilitybilling.consumerservice.exception.NotFoundException;
import com.utilitybilling.consumerservice.feign.NotificationClient;
import com.utilitybilling.consumerservice.feign.NotificationRequest;
import com.utilitybilling.consumerservice.model.ConsumerRequest;
import com.utilitybilling.consumerservice.repository.ConsumerRequestRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumerRequestService {

	private final ConsumerRequestRepository repository;
	private final NotificationClient notificationClient;

	private static final String STATUS_PENDING = "PENDING";

	public ConsumerRequestResponse submit(CreateConsumerRequest r) {

		boolean exists = repository.existsByEmailAndStatusIn(r.getEmail(), List.of(STATUS_PENDING, "APPROVED"));

		if (exists)
			throw new IllegalStateException("Consumer request already exists for this email");

		ConsumerRequest cr = ConsumerRequest.builder().fullName(r.getFullName()).email(r.getEmail()).phone(r.getPhone())
				.addressLine1(r.getAddressLine1()).city(r.getCity()).state(r.getState()).postalCode(r.getPostalCode())
				.status(STATUS_PENDING).createdAt(Instant.now()).updatedAt(Instant.now()).build();

		cr = repository.save(cr);

		return ConsumerRequestResponse.builder().requestId(cr.getId()).status(cr.getStatus()).build();
	}

	public Page<ConsumerRequest> getAll(String status, int page, int size) {
		PageRequest pr = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return status == null ? repository.findAll(pr) : repository.findByStatus(status, pr);
	}

	public ConsumerRequest getById(String id) {
		return repository.findById(id).orElseThrow(() -> new NotFoundException("Consumer request not found"));
	}

	public void reject(String id, String reason) {
		ConsumerRequest r = getById(id);

		if (!STATUS_PENDING.equals(r.getStatus()))
			throw new IllegalStateException("Request already processed");

		r.setStatus("REJECTED");
		r.setRejectionReason(reason);
		r.setUpdatedAt(Instant.now());

		notificationClient.send(NotificationRequest.builder().email(r.getEmail()).type("CONSUMER_REJECTED")
				.subject("Consumer Request rejected")
				.message("Your request for consumer has been rejected" + "\n Reason for rejection: " + reason).build());

		repository.save(r);
	}
}
