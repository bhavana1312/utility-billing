package com.utilitybilling.meterservice.service;

import com.utilitybilling.meterservice.client.NotificationClient;
import com.utilitybilling.meterservice.client.NotificationRequest;
import com.utilitybilling.meterservice.dto.*;
import com.utilitybilling.meterservice.model.*;
import com.utilitybilling.meterservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterService {

	private final ConnectionRequestRepository connectionRepo;
	private final MeterRepository meterRepo;
	private final MeterReadingRepository readingRepo;
	private final NotificationClient notificationClient;

	public void requestConnection(CreateConnectionRequest request) {

		boolean exists = connectionRepo.existsByConsumerIdAndStatusIn(request.getConsumerId(),
				List.of("PENDING", "APPROVED"));

		if (exists)
			throw new IllegalStateException("Consumer request already exists");
		ConnectionRequest cr = new ConnectionRequest();
		cr.setConsumerId(request.getConsumerId());
		cr.setEmail(request.getEmail());
		cr.setUtilityType(request.getUtilityType());
		cr.setAddress(request.getAddress());
		connectionRepo.save(cr);
	}

	public List<ConnectionRequest> getAllRequests() {
		return connectionRepo.findAll();
	}

	public void approve(String id) {
		ConnectionRequest cr = connectionRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Connection request not found"));

		if (cr.getStatus() != ConnectionStatus.PENDING)
			throw new IllegalStateException("Only pending requests can be approved");

		meterRepo.findByConsumerIdAndUtilityTypeAndActiveTrue(cr.getConsumerId(), cr.getUtilityType()).ifPresent(m -> {
			throw new IllegalStateException("Meter already exists");
		});

		Meter m = new Meter();
		m.setConsumerId(cr.getConsumerId());
		m.setEmail(cr.getEmail());
		m.setUtilityType(cr.getUtilityType());
		m.setInstallationDate(Instant.now());
		m.setLastReading(0);
		m.setActive(true);
		meterRepo.save(m);

		cr.setStatus(ConnectionStatus.APPROVED);
		connectionRepo.save(cr);

		notificationClient
				.send(NotificationRequest.builder().email(cr.getEmail()).type("CONNECTION_APPROVED")
						.subject("Utility connection approved").message("Your " + cr.getUtilityType()
								+ " connection has been approved.\n\n" + "Meter Number: " + m.getMeterNumber())
						.build());
	}

	public void reject(String id, String reason) {
		ConnectionRequest cr = connectionRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Connection request not found"));

		if (cr.getStatus() != ConnectionStatus.PENDING)
			throw new IllegalStateException("Only pending requests can be rejected");

		cr.setStatus(ConnectionStatus.REJECTED);
		cr.setRejectionReason(reason);
		connectionRepo.save(cr);

		notificationClient.send(NotificationRequest.builder().email(cr.getEmail()).type("CONNECTION_REJECTED")
				.subject("Utility connection request rejected")
				.message("Your " + cr.getUtilityType() + " connection request was rejected.\n\n" + "Reason: " + reason)
				.build());
	}

	public MeterDetailsResponse getMeter(String meterNumber) {
		Meter m = meterRepo.findById(meterNumber).orElseThrow(() -> new IllegalArgumentException("Meter not found"));

		MeterDetailsResponse r = new MeterDetailsResponse();
		r.setMeterNumber(m.getMeterNumber());
		r.setEmail(m.getEmail());
		r.setConsumerId(m.getConsumerId());
		r.setUtilityType(m.getUtilityType());
		r.setActive(m.isActive());
		r.setLastReading(m.getLastReading());
		return r;
	}

	public List<Meter> getMetersByConsumer(String consumerId) {
		return meterRepo.findByConsumerId(consumerId);
	}

	public void deactivateMeter(String meterNumber) {
		Meter m = meterRepo.findById(meterNumber).orElseThrow(() -> new IllegalArgumentException("Meter not found"));
		m.setActive(false);
		meterRepo.save(m);
	}

	public MeterReadingResponse addReading(CreateMeterReadingRequest request) {
		Meter meter = meterRepo.findById(request.getMeterNumber())
				.orElseThrow(() -> new IllegalArgumentException("Meter not found"));

		if (!meter.isActive())
			throw new IllegalStateException("Meter is inactive");

		if (request.getReadingValue() <= meter.getLastReading())
			throw new IllegalStateException("Reading must be greater than last reading");

		double previous = meter.getLastReading();

		MeterReading mr = new MeterReading();
		mr.setMeterNumber(request.getMeterNumber());
		mr.setReadingValue(request.getReadingValue());
		readingRepo.save(mr);

		meter.setLastReading(request.getReadingValue());
		meterRepo.save(meter);

		MeterReadingResponse r = new MeterReadingResponse();
		r.setMeterNumber(request.getMeterNumber());
		r.setPreviousReading(previous);
		r.setCurrentReading(request.getReadingValue());
		r.setUnitsUsed(request.getReadingValue() - previous);
		return r;
	}

	public double getLastReading(String meterNumber) {
		return meterRepo.findById(meterNumber).orElseThrow(() -> new IllegalArgumentException("Meter not found"))
				.getLastReading();
	}

	public long getMeterCount() {
		return meterRepo.count();
	}

}
