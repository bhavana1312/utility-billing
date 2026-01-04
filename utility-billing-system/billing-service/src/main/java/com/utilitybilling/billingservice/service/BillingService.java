package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.dto.*;
import com.utilitybilling.billingservice.feign.*;
import com.utilitybilling.billingservice.model.*;
import com.utilitybilling.billingservice.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingService {

	private final MeterClient meterClient;
	private final ConsumerClient consumerClient;
	private final TariffGateway tariffGateway;
	private final BillRepository billRepo;
	private final NotificationClient notificationClient;

	public BillResponse generate(GenerateBillRequest request) {

		MeterResponse meter = meterClient.getMeter(request.getMeterNumber());

		if (!meter.isActive())
			throw new IllegalStateException("Meter is inactive");

		ConsumerResponse consumer = consumerClient.get(meter.getConsumerId());

		if (consumer == null)
			throw new IllegalArgumentException("Consumer does not exist");

		double latestReading = meterClient.getLastReading(request.getMeterNumber());

		double previousReading = billRepo.findTopByMeterNumberOrderByGeneratedAtDesc(request.getMeterNumber())
				.map(Bill::getCurrentReading).orElse(0.0);

		if (latestReading <= previousReading)
			throw new IllegalStateException("No new consumption since last bill");

		double units = latestReading - previousReading;

		TariffResponse tariff = tariffGateway.getActive(meter.getUtilityType(), meter.getTariffPlan());

		BigDecimal energyCharge = calculateEnergyCharge(units, tariff.getSlabs());
		BigDecimal fixedCharge = BigDecimal.valueOf(tariff.getFixedCharge());

		BigDecimal taxAmount = energyCharge.add(fixedCharge).multiply(BigDecimal.valueOf(tariff.getTaxPercentage()))
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

		BigDecimal totalAmount = energyCharge.add(fixedCharge).add(taxAmount).setScale(2, RoundingMode.HALF_UP);

		Bill bill = new Bill();
		bill.setConsumerId(meter.getConsumerId());
		bill.setMeterNumber(request.getMeterNumber());
		bill.setUtilityType(meter.getUtilityType());
		bill.setTariffPlan(meter.getTariffPlan());
		bill.setPreviousReading(previousReading);
		bill.setCurrentReading(latestReading);
		bill.setUnitsConsumed(units);
		bill.setEnergyCharge(energyCharge);
		bill.setFixedCharge(fixedCharge);
		bill.setTaxAmount(taxAmount);
		bill.setPenaltyAmount(BigDecimal.ZERO);
		bill.setTotalAmount(totalAmount);
		bill.setGeneratedAt(Instant.now());
		bill.setDueDate(Date.from(bill.getGeneratedAt().plus(2, ChronoUnit.DAYS)));
		bill.setLastUpdatedAt(Instant.now());
		bill.setStatus(BillStatus.DUE);

		Bill savedBill = billRepo.save(bill);

		notificationClient.send(NotificationRequest.builder().email(consumer.getEmail()).type("BILL_GENERATED")
				.subject("Your utility bill is ready")
				.message("Your " + bill.getUtilityType() + " bill has been generated.\n\n" + "Bill ID: "
						+ savedBill.getId() + "\n" + "Amount Due: ₹" + bill.getTotalAmount() + "\n" + "Due Date: "
						+ bill.getDueDate())
				.build());

		return map(savedBill);
	}

	public Page<BillResponse> consumerBills(String consumerId, int page, int size) {
		return billRepo.findByConsumerIdOrderByGeneratedAtDesc(consumerId,
				PageRequest.of(page, size, Sort.by("generatedAt").descending())).map(this::map);
	}

	public Page<BillResponse> all(BillStatus status, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("generatedAt").descending());
		return (status == null ? billRepo.findAll(pageable) : billRepo.findByStatus(status, pageable)).map(this::map);
	}

	public Bill getById(String billId) {
		return billRepo.findById(billId).orElseThrow(() -> new IllegalArgumentException("Bill not found"));
	}

	public OutstandingBalanceResponse outstanding(String consumerId) {

		List<Bill> bills = billRepo.findByConsumerIdAndStatusIn(consumerId,
				List.of(BillStatus.DUE, BillStatus.OVERDUE));

		BigDecimal totalOutstanding = bills.stream().map(Bill::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

		OutstandingBalanceResponse r = new OutstandingBalanceResponse();
		r.setConsumerId(consumerId);
		r.setOutstandingAmount(totalOutstanding);

		return r;
	}

	public void markPaid(String billId) {
		Bill bill = billRepo.findById(billId).orElseThrow(() -> new IllegalArgumentException("Bill not found"));
		bill.setStatus(BillStatus.PAID);
		bill.setLastUpdatedAt(Instant.now());
		billRepo.save(bill);
	}

	private BigDecimal calculateEnergyCharge(double units, Iterable<TariffSlab> slabs) {

		BigDecimal amount = BigDecimal.ZERO;
		double remaining = units;

		for (TariffSlab slab : slabs) {
			if (remaining <= 0)
				break;

			int slabUnits = slab.getToUnit() - slab.getFromUnit() + 1;
			double used = Math.min(remaining, slabUnits);

			BigDecimal slabCharge = BigDecimal.valueOf(used).multiply(BigDecimal.valueOf(slab.getRatePerUnit()));

			amount = amount.add(slabCharge);
			remaining -= used;
		}

		return amount.setScale(2, RoundingMode.HALF_UP);
	}

	private BillResponse map(Bill bill) {
		BillResponse r = new BillResponse();
		r.setBillId(bill.getId());
		r.setConsumerId(bill.getConsumerId());
		r.setMeterNumber(bill.getMeterNumber());
		r.setUtilityType(bill.getUtilityType());
		r.setTariffPlan(bill.getTariffPlan());
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
}
