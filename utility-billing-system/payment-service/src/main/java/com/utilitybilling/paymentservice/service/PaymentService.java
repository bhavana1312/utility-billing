package com.utilitybilling.paymentservice.service;

import com.utilitybilling.paymentservice.client.BillResponse;
import com.utilitybilling.paymentservice.client.BillStatus;
import com.utilitybilling.paymentservice.client.BillingClient;
import com.utilitybilling.paymentservice.dto.*;
import com.utilitybilling.paymentservice.model.*;
import com.utilitybilling.paymentservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepo;
	private final InvoiceRepository invoiceRepo;
	private final BillingClient billingClient;

	public Object initiate(InitiatePaymentRequest request) {
		BillResponse bill = billingClient.getBill(request.getBillId());
		System.out.print(bill);

		if (!bill.getStatus().equals(BillStatus.DUE) && !bill.getStatus().equals(BillStatus.OVERDUE))
			throw new RuntimeException("Bill is not payable");

		String otp = String.valueOf(100000 + new Random().nextInt(900000));

		Payment p = new Payment();
		p.setBillId(bill.getId());
		p.setConsumerId(bill.getConsumerId());
		p.setAmount(bill.getTotalAmount());
		p.setMode(PaymentMode.ONLINE);
		p.setStatus(PaymentStatus.INITIATED);
		p.setOtp(otp);
		p.setOtpExpiresAt(Instant.now().plusSeconds(300));
		p.setProcessedBy("SYSTEM");

		paymentRepo.save(p);

		return p;
	}

	public Object confirm(ConfirmPaymentRequest request) {
		Payment p = paymentRepo.findById(request.getPaymentId())
				.orElseThrow(() -> new RuntimeException("Payment not found"));

		if (p.getStatus() != PaymentStatus.INITIATED)
			throw new RuntimeException("Payment already processed");

		if (!p.getOtp().equals(request.getOtp()) || Instant.now().isAfter(p.getOtpExpiresAt())) {
			p.setStatus(PaymentStatus.FAILED);
			paymentRepo.save(p);
			throw new RuntimeException("Invalid or expired OTP");
		}

		billingClient.markPaid(p.getBillId());

		p.setStatus(PaymentStatus.SUCCESS);
		p.setCompletedAt(Instant.now());
		paymentRepo.save(p);

		Invoice inv = new Invoice();
		inv.setPaymentId(p.getId());
		inv.setBillId(p.getBillId());
		inv.setConsumerId(p.getConsumerId());
		inv.setAmount(p.getAmount());
		inv.setMode(p.getMode());
		invoiceRepo.save(inv);

		return inv;
	}

	public void offlinePay(OfflinePaymentRequest request) {
		BillResponse bill = billingClient.getBill(request.getBillId());

		if (!bill.getStatus().equals(BillStatus.DUE) && !bill.getStatus().equals(BillStatus.OVERDUE))
			throw new RuntimeException("Bill is not payable");

		Payment p = new Payment();
		p.setBillId(bill.getId());
		p.setConsumerId(bill.getConsumerId());
		p.setAmount(bill.getTotalAmount());
		p.setMode(request.getMode());
		p.setStatus(PaymentStatus.SUCCESS);
		p.setProcessedBy("PAYMENT_OFFICER");
		p.setCompletedAt(Instant.now());

		paymentRepo.save(p);
		billingClient.markPaid(bill.getId());

		Invoice inv = new Invoice();
		inv.setPaymentId(p.getId());
		inv.setBillId(p.getBillId());
		inv.setConsumerId(p.getConsumerId());
		inv.setAmount(p.getAmount());
		inv.setMode(p.getMode());
		invoiceRepo.save(inv);
	}

	public List<Payment> history(String consumerId) {
		return paymentRepo.findByConsumerId(consumerId);
	}

	public List<Invoice> invoices(String consumerId) {
		return invoiceRepo.findByConsumerId(consumerId);
	}

	public Object outstanding(String consumerId) {
		return billingClient.outstanding(consumerId);
	}
}
