package com.utilitybilling.paymentservice.config;

import com.utilitybilling.paymentservice.feign.BillingClient;
import com.utilitybilling.paymentservice.feign.BillResponse;
import com.utilitybilling.paymentservice.feign.BillStatus;
import com.utilitybilling.paymentservice.model.Invoice;
import com.utilitybilling.paymentservice.model.Payment;
import com.utilitybilling.paymentservice.model.PaymentMode;
import com.utilitybilling.paymentservice.model.PaymentStatus;
import com.utilitybilling.paymentservice.repository.InvoiceRepository;
import com.utilitybilling.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class PaymentInvoiceSeedDataInitializer {

	private final PaymentRepository paymentRepo;
	private final InvoiceRepository invoiceRepo;
	private final BillingClient billingClient;

	public void seedPayments() {

		if (paymentRepo.count() > 0 || invoiceRepo.count() > 0)
			return;

		List<BillResponse> bills;
		try {
			bills = billingClient.getAllBills();
		} catch (Exception e) {
			return;
		}

		int paidCount = 0;
		int maxPaid = 60;

		for (BillResponse bill : bills) {

			if (paidCount >= maxPaid)
				break;

			if (bill.getStatus() != BillStatus.DUE)
				continue;

			if (paymentRepo.findAll().stream().anyMatch(p -> bill.getBillId().equals(p.getBillId())))
				continue;

			Payment p = new Payment();
			p.setBillId(bill.getBillId());
			p.setConsumerId(bill.getConsumerId());
			p.setUtilityType(bill.getUtilityType());
			p.setAmount(bill.getTotalAmount());
			p.setMode(PaymentMode.ONLINE);
			p.setStatus(PaymentStatus.SUCCESS);
			p.setProcessedBy("SYSTEM");
			p.setCompletedAt(randomCompletedAt());

			p = paymentRepo.save(p);

			billingClient.markPaid(bill.getBillId());

			BillResponse updatedBill = billingClient.getBill(bill.getBillId());

			Invoice inv = new Invoice();
			inv.setPaymentId(p.getId());
			inv.setBillId(updatedBill.getBillId());
			inv.setConsumerId(updatedBill.getConsumerId());
			inv.setMeterNumber(updatedBill.getMeterNumber());
			inv.setUtilityType(updatedBill.getUtilityType());

			inv.setPreviousReading(updatedBill.getPreviousReading());
			inv.setCurrentReading(updatedBill.getCurrentReading());
			inv.setUnitsConsumed(updatedBill.getUnitsConsumed());

			inv.setEnergyCharge(updatedBill.getEnergyCharge());
			inv.setFixedCharge(updatedBill.getFixedCharge());
			inv.setTaxAmount(updatedBill.getTaxAmount());
			inv.setPenaltyAmount(updatedBill.getPenaltyAmount());
			inv.setTotalAmount(updatedBill.getTotalAmount());

			inv.setPaymentMode(p.getMode());
			inv.setPaymentDate(p.getCompletedAt());

			inv.setBillGeneratedAt(updatedBill.getGeneratedAt());
			inv.setBillDueDate(updatedBill.getDueDate());

			invoiceRepo.save(inv);

			paidCount++;
		}
	}

	private Instant randomCompletedAt() {

		long start = Instant.parse("2025-10-01T00:00:00Z").getEpochSecond();
		long end = Instant.parse("2025-12-31T23:59:59Z").getEpochSecond();

		long randomEpoch = start + (long) (Math.random() * (end - start));
		return Instant.ofEpochSecond(randomEpoch);
	}
}
