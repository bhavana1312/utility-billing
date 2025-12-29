package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.dto.BillResponse;
import com.utilitybilling.billingservice.dto.OutstandingBalanceResponse;
import com.utilitybilling.billingservice.model.*;
import com.utilitybilling.billingservice.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingQueryService {

	private final BillRepository billRepo;

	public List<BillResponse> consumerBills(String consumerId) {
		return billRepo.findByConsumerIdOrderByGeneratedAtDesc(consumerId).stream().map(this::map).toList();
	}

	public List<BillResponse> all(BillStatus status) {
		List<Bill> bills = status == null ? billRepo.findAll() : billRepo.findByStatus(status);

		return bills.stream().map(this::map).toList();
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

	public Bill getById(String billId) {
		Bill bill = billRepo.findById(billId).orElseThrow(() -> new RuntimeException("Bill not found"));

		return bill;
	}

	public OutstandingBalanceResponse outstanding(String consumerId) {

		List<Bill> bills = billRepo.findByConsumerIdAndStatusIn(consumerId,
				List.of(BillStatus.DUE, BillStatus.OVERDUE));

		double total = bills.stream().mapToDouble(Bill::getTotalAmount).sum();

		OutstandingBalanceResponse r = new OutstandingBalanceResponse();
		r.setConsumerId(consumerId);
		r.setOutstandingAmount(total);

		return r;
	}
}
