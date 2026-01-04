package com.utilitybilling.meterservice.service;

import com.utilitybilling.meterservice.dto.*;
import com.utilitybilling.meterservice.feign.ConsumerClient;
import com.utilitybilling.meterservice.feign.ConsumerResponse;
import com.utilitybilling.meterservice.feign.NotificationClient;
import com.utilitybilling.meterservice.feign.NotificationRequest;
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
	private final ConsumerClient consumerClient;
	private static final String METER_NOT_FOUND = "Meter not found";

	public void requestConnection(CreateConnectionRequest request) {

		boolean exists = connectionRepo.existsByConsumerIdAndStatusInAndUtilityType(request.getConsumerId(),
				List.of(ConnectionStatus.PENDING, ConnectionStatus.APPROVED), request.getUtilityType());

		if (exists)
			throw new IllegalStateException("Consumer request already exists");
		ConnectionRequest cr = new ConnectionRequest();
		cr.setConsumerId(request.getConsumerId());
		cr.setUtilityType(request.getUtilityType());
		cr.setTariffPlan(request.getTariffPlan());
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
		m.setUtilityType(cr.getUtilityType());
		m.setTariffPlan(cr.getTariffPlan());
		m.setInstallationDate(Instant.now());
		m.setLastReading(0);
		m.setActive(true);
		meterRepo.save(m);

		cr.setStatus(ConnectionStatus.APPROVED);
		connectionRepo.save(cr);

		ConsumerResponse consumer = consumerClient.get(cr.getConsumerId());

		try {
			notificationClient.send(NotificationRequest.builder().email(consumer.getEmail()).type("CONNECTION_APPROVED")
					.subject("Utility connection approved")
					.message("Your " + cr.getUtilityType() + " connection has been approved.\n" + "Plan: "
							+ cr.getTariffPlan() + "\n" + "Meter Number: " + m.getMeterNumber())
					.build());
		} catch (Exception e) {
			// Notification failure should not affect connection approval flow
		}
	}

	public void reject(String id, String reason) {

		ConnectionRequest cr = connectionRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Connection request not found"));

		if (cr.getStatus() != ConnectionStatus.PENDING)
			throw new IllegalStateException("Only pending requests can be rejected");

		cr.setStatus(ConnectionStatus.REJECTED);
		cr.setRejectionReason(reason);
		connectionRepo.save(cr);

		ConsumerResponse consumer = consumerClient.get(cr.getConsumerId());

		try {
			notificationClient.send(NotificationRequest.builder().email(consumer.getEmail()).type("CONNECTION_REJECTED")
					.subject("Utility connection request rejected").message("Your " + cr.getUtilityType()
							+ " connection request was rejected.\n\n" + "Reason: " + reason)
					.build());
		} catch (Exception e) {
			// Notification failure should not affect rejection flow
		}
	}

	public MeterDetailsResponse getMeter(String meterNumber) {
		Meter m = meterRepo.findById(meterNumber).orElseThrow(() -> new IllegalArgumentException(METER_NOT_FOUND));

		MeterDetailsResponse r = new MeterDetailsResponse();
		r.setMeterNumber(m.getMeterNumber());
		r.setConsumerId(m.getConsumerId());
		r.setUtilityType(m.getUtilityType());
		r.setTariffPlan(m.getTariffPlan());
		r.setActive(m.isActive());
		r.setLastReading(m.getLastReading());
		return r;
	}

	public List<Meter> getMetersByConsumer(String consumerId) {
		return meterRepo.findByConsumerId(consumerId);
	}

	public void deactivateMeter(String meterNumber) {
		Meter m = meterRepo.findById(meterNumber).orElseThrow(() -> new IllegalArgumentException(METER_NOT_FOUND));
		m.setActive(false);
		meterRepo.save(m);
	}

	public MeterReadingResponse addReading(CreateMeterReadingRequest request) {
		Meter meter = meterRepo.findById(request.getMeterNumber())
				.orElseThrow(() -> new IllegalArgumentException(METER_NOT_FOUND));

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
		return meterRepo.findById(meterNumber).orElseThrow(() -> new IllegalArgumentException(METER_NOT_FOUND))
				.getLastReading();
	}

	public List<Meter> getAllMeters() {
		return meterRepo.findAll();
	}

	public List<Meter> getMyMeters(String username) {
		ConsumerResponse c = consumerClient.getByUsername(username);
		return meterRepo.findByConsumerId(c.getId());
	}

}
