package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.dto.*;
import com.utilitybilling.billingservice.feign.*;
import com.utilitybilling.billingservice.model.*;
import com.utilitybilling.billingservice.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BillingService {

	private final MeterClient meterClient;
	private final TariffClient tariffClient;
	private final ConsumerClient consumerClient;
	private final BillRepository billRepo;

	public BillResponse generate(GenerateBillRequest request) {

		MeterResponse meter = meterClient.getMeter(request.getMeterNumber());

		if (!meter.isActive())
			throw new IllegalStateException("Meter is inactive");

		ConsumerExistsResponse exists = consumerClient.exists(meter.getConsumerId());

		if (!exists.isExists())
			throw new IllegalArgumentException("Consumer does not exist");

		double currentReading = meterClient.getLastReading(request.getMeterNumber());

		double previousReading = billRepo.findTopByMeterNumberOrderByGeneratedAtDesc(request.getMeterNumber())
				.map(Bill::getCurrentReading).orElse(0.0);

		if (currentReading <= previousReading)
			throw new IllegalStateException("No new consumption since last bill");

		double units = currentReading - previousReading;

		TariffResponse tariff = tariffClient.getActive(meter.getUtilityType());

		double energyCharge = calculateEnergyCharge(units, tariff.getSlabs());

		double fixedCharge = tariff.getFixedCharge();
		double tax = (energyCharge + fixedCharge) * tariff.getTaxPercentage() / 100.0;

		Bill bill = new Bill();
		bill.setConsumerId(meter.getConsumerId());
		bill.setMeterNumber(request.getMeterNumber());
		bill.setUtilityType(meter.getUtilityType());
		bill.setPreviousReading(previousReading);
		bill.setCurrentReading(currentReading);
		bill.setUnitsConsumed(units);
		bill.setEnergyCharge(energyCharge);
		bill.setFixedCharge(fixedCharge);
		bill.setTaxAmount(tax);
		bill.setPenaltyAmount(0);
		bill.setTotalAmount(energyCharge + fixedCharge + tax);
		bill.setDueDate(bill.getGeneratedAt().plus(15, ChronoUnit.DAYS));
		bill.setLastUpdatedAt(Instant.now());
		bill.setStatus(BillStatus.DUE);

		return map(billRepo.save(bill));
	}

	private double calculateEnergyCharge(double units, Iterable<TariffSlab> slabs) {

		double remaining = units;
		double amount = 0;

		for (TariffSlab slab : slabs) {
			if (remaining <= 0)
				break;

			int slabUnits = slab.getToUnit() - slab.getFromUnit() + 1;

			double used = Math.min(remaining, slabUnits);
			amount += used * slab.getRatePerUnit();
			remaining -= used;
		}
		return amount;
	}

	private BillResponse map(Bill bill) {
		BillResponse r = new BillResponse();
		r.setBillId(bill.getId());
		r.setMeterNumber(bill.getMeterNumber());
		r.setUtilityType(bill.getUtilityType());
		r.setPreviousReading(bill.getPreviousReading());
		r.setCurrentReading(bill.getCurrentReading());
		r.setUnitsConsumed(bill.getUnitsConsumed());
		r.setEnergyCharge(bill.getEnergyCharge());
		r.setFixedCharge(bill.getFixedCharge());
		r.setTaxAmount(bill.getTaxAmount());
		r.setPenaltyAmount(bill.getPenaltyAmount());
		r.setTotalAmount(bill.getTotalAmount());
		r.setStatus(bill.getStatus().name());
		r.setGeneratedAt(bill.getGeneratedAt());
		r.setDueDate(bill.getDueDate());
		return r;
	}

	public void markPaid(String billId) {
		Bill bill = billRepo.findById(billId).orElseThrow(() -> new IllegalArgumentException("Bill not found"));

		bill.setStatus(BillStatus.PAID);
		bill.setLastUpdatedAt(Instant.now());
		billRepo.save(bill);
	}
}
