package com.utilitybilling.paymentservice.service;

import com.utilitybilling.paymentservice.client.*;
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

	public Payment initiate(InitiatePaymentRequest request) {

		BillResponse bill = billingClient.getBill(request.getBillId());

		if (!(bill.getStatus() == BillStatus.DUE || bill.getStatus() == BillStatus.OVERDUE))
			throw new IllegalStateException("Bill is not payable");

		paymentRepo.findByBillIdAndStatus(bill.getId(), PaymentStatus.SUCCESS).ifPresent(p -> {
			throw new IllegalStateException("Bill already paid");
		});

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

		return paymentRepo.save(p);
	}

	public Invoice confirm(ConfirmPaymentRequest request) {

		Payment p = paymentRepo.findById(request.getPaymentId())
				.orElseThrow(() -> new IllegalArgumentException("Payment not found"));

		if (p.getStatus() != PaymentStatus.INITIATED)
			throw new IllegalStateException("Payment already processed");

		if (!p.getOtp().equals(request.getOtp()) || Instant.now().isAfter(p.getOtpExpiresAt())) {
			p.setStatus(PaymentStatus.FAILED);
			paymentRepo.save(p);
			throw new IllegalArgumentException("Invalid or expired OTP");
		}

		billingClient.markPaid(p.getBillId());

		p.setStatus(PaymentStatus.SUCCESS);
		p.setCompletedAt(Instant.now());
		paymentRepo.save(p);

		Invoice i = new Invoice();
		i.setPaymentId(p.getId());
		i.setBillId(p.getBillId());
		i.setConsumerId(p.getConsumerId());
		i.setAmount(p.getAmount());
		i.setMode(p.getMode());

		return invoiceRepo.save(i);
	}

	public void offlinePay(OfflinePaymentRequest request) {

		BillResponse bill = billingClient.getBill(request.getBillId());

		if (!(bill.getStatus() == BillStatus.DUE || bill.getStatus() == BillStatus.OVERDUE))
			throw new IllegalStateException("Bill is not payable");

		billingClient.markPaid(bill.getId());

		Payment p = new Payment();
		p.setBillId(bill.getId());
		p.setConsumerId(bill.getConsumerId());
		p.setAmount(bill.getTotalAmount());
		p.setMode(request.getMode());
		p.setStatus(PaymentStatus.SUCCESS);
		p.setProcessedBy("PAYMENT_OFFICER");
		p.setCompletedAt(Instant.now());

		paymentRepo.save(p);

		Invoice i = new Invoice();
		i.setPaymentId(p.getId());
		i.setBillId(p.getBillId());
		i.setConsumerId(p.getConsumerId());
		i.setAmount(p.getAmount());
		i.setMode(p.getMode());

		invoiceRepo.save(i);
	}

	public List<Payment> history(String consumerId) {
		return paymentRepo.findByConsumerId(consumerId);
	}

	public List<Invoice> invoices(String consumerId) {
		return invoiceRepo.findByConsumerId(consumerId);
	}

	public OutstandingBalanceResponse outstanding(String consumerId) {
		return billingClient.outstanding(consumerId);
	}
}
