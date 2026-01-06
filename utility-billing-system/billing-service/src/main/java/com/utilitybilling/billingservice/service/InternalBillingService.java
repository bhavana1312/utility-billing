package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.dto.*;
import com.utilitybilling.billingservice.model.*;
import com.utilitybilling.billingservice.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InternalBillingService {

	private final BillRepository billRepo;

	public BillResponse getById(String billId) {
		Bill bill = billRepo.findById(billId)
				.orElseThrow(() -> new IllegalArgumentException("Bill not found"));
		return map(bill);
	}

	public void markPaid(String billId) {
		Bill bill = billRepo.findById(billId)
				.orElseThrow(() -> new IllegalArgumentException("Bill not found"));

		bill.setStatus(BillStatus.PAID);
		bill.setLastUpdatedAt(Instant.now());
		billRepo.save(bill);
	}

	public OutstandingBalanceResponse outstanding(String consumerId) {

		List<Bill> bills = billRepo.findByConsumerIdAndStatusIn(
				consumerId,
				List.of(BillStatus.DUE, BillStatus.OVERDUE)
		);

		BigDecimal total = bills.stream()
				.map(Bill::getTotalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		OutstandingBalanceResponse r = new OutstandingBalanceResponse();
		r.setConsumerId(consumerId);
		r.setOutstandingAmount(total);
		return r;
	}

	public List<BillResponse> allBills() {
		return billRepo.findAll().stream().map(this::map).toList();
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
