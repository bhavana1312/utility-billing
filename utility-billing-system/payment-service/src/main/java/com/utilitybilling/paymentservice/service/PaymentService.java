package com.utilitybilling.paymentservice.service;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.utilitybilling.paymentservice.dto.ConfirmPaymentRequest;
import com.utilitybilling.paymentservice.dto.InitiatePaymentRequest;
import com.utilitybilling.paymentservice.dto.InvoicePdfData;
import com.utilitybilling.paymentservice.dto.OfflinePaymentRequest;
import com.utilitybilling.paymentservice.feign.BillResponse;
import com.utilitybilling.paymentservice.feign.BillStatus;
import com.utilitybilling.paymentservice.feign.BillingClient;
import com.utilitybilling.paymentservice.feign.ConsumerClient;
import com.utilitybilling.paymentservice.feign.ConsumerResponse;
import com.utilitybilling.paymentservice.feign.NotificationClient;
import com.utilitybilling.paymentservice.feign.NotificationRequest;
import com.utilitybilling.paymentservice.model.Invoice;
import com.utilitybilling.paymentservice.model.Payment;
import com.utilitybilling.paymentservice.model.PaymentMode;
import com.utilitybilling.paymentservice.model.PaymentStatus;
import com.utilitybilling.paymentservice.repository.InvoiceRepository;
import com.utilitybilling.paymentservice.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepo;
	private final InvoiceRepository invoiceRepo;
	private final BillingClient billingClient;
	private final ConsumerClient consumerClient;
	private final NotificationClient notificationClient;
	private final InvoicePdfService invoicePdfService;

	private static final Random RANDOM = new Random();

	public Object initiate(InitiatePaymentRequest request) {
		BillResponse bill = billingClient.getBill(request.getBillId());

		if (!bill.getStatus().equals(BillStatus.DUE) && !bill.getStatus().equals(BillStatus.OVERDUE))
			throw new IllegalStateException("Bill is not payable");

		String otp = String.valueOf(100000 + RANDOM.nextInt(900000));

		Payment p = new Payment();
		p.setBillId(bill.getId());
		p.setUtilityType(bill.getUtilityType());
		p.setConsumerId(bill.getConsumerId());
		p.setAmount(bill.getTotalAmount());
		p.setMode(PaymentMode.ONLINE);
		p.setStatus(PaymentStatus.INITIATED);
		p.setOtp(otp);
		p.setOtpExpiresAt(Instant.now().plusSeconds(300));
		p.setProcessedBy("SYSTEM");

		paymentRepo.save(p);

		ConsumerResponse consumer = consumerClient.get(bill.getConsumerId());

		notificationClient.send(NotificationRequest.builder().email(consumer.getEmail()).type("PAYMENT_OTP")
				.subject("OTP for " + p.getUtilityType() + " bill payment")
				.message("Payment initiated with id: " + p.getId() + "\n" + "Your OTP for " + p.getUtilityType()
						+ " paying bill with id " + bill.getId() + " is: " + otp + "\n\n"
						+ "This OTP is valid for 5 minutes.")
				.build());

		return p;
	}

	public Object confirm(ConfirmPaymentRequest request) {
		Payment p = paymentRepo.findById(request.getPaymentId())
				.orElseThrow(() -> new IllegalArgumentException("Payment not found"));

		if (p.getStatus() != PaymentStatus.INITIATED)
			throw new IllegalStateException("Payment already processed");

		if (!p.getOtp().equals(request.getOtp()) || Instant.now().isAfter(p.getOtpExpiresAt())) {
			p.setStatus(PaymentStatus.FAILED);
			paymentRepo.save(p);

			ConsumerResponse consumer = consumerClient.get(p.getConsumerId());

			notificationClient
					.send(NotificationRequest.builder().email(consumer.getEmail()).type("PAYMENT_FAILED")
							.subject("Payment failed").message("Your payment for " + p.getUtilityType()
									+ " bill with id: " + p.getBillId() + " failed due to invalid or expired OTP.")
							.build());

			throw new IllegalArgumentException("Invalid or expired OTP");
		}

		billingClient.markPaid(p.getBillId());

		p.setStatus(PaymentStatus.SUCCESS);
		p.setCompletedAt(Instant.now());
		paymentRepo.save(p);

		BillResponse bill = billingClient.getBill(p.getBillId());

		Invoice inv = new Invoice();

		inv.setPaymentId(p.getId());
		inv.setBillId(bill.getId());
		inv.setConsumerId(bill.getConsumerId());
		inv.setMeterNumber(bill.getMeterNumber());
		inv.setUtilityType(bill.getUtilityType());

		inv.setPreviousReading(bill.getPreviousReading());
		inv.setCurrentReading(bill.getCurrentReading());
		inv.setUnitsConsumed(bill.getUnitsConsumed());

		inv.setEnergyCharge(bill.getEnergyCharge());
		inv.setFixedCharge(bill.getFixedCharge());
		inv.setTaxAmount(bill.getTaxAmount());
		inv.setPenaltyAmount(bill.getPenaltyAmount());
		inv.setTotalAmount(bill.getTotalAmount());

		inv.setPaymentMode(p.getMode());
		inv.setPaymentDate(Instant.now());

		inv.setBillDueDate(bill.getDueDate());
		inv.setBillGeneratedAt(bill.getGeneratedAt());

		invoiceRepo.save(inv);
		ConsumerResponse consumer = consumerClient.get(bill.getConsumerId());

		InvoicePdfData pdfData = toPdfData(inv);
		byte[] pdf = invoicePdfService.generate(pdfData);
		String base64 = Base64.getEncoder().encodeToString(pdf);

		notificationClient.send(NotificationRequest.builder().email(consumer.getEmail()).type("INVOICE_PDF")
				.subject("Invoice for " + inv.getUtilityType() + " Bill")
				.message("Please find attached your invoice for payment ID: " + inv.getPaymentId())
				.attachmentBase64(base64).attachmentName("invoice-" + inv.getId() + ".pdf").build());

		return inv;
	}

	public void offlinePay(OfflinePaymentRequest request) {
		BillResponse bill = billingClient.getBill(request.getBillId());

		if (!bill.getStatus().equals(BillStatus.DUE) && !bill.getStatus().equals(BillStatus.OVERDUE))
			throw new IllegalStateException("Bill is not payable");

		Payment p = new Payment();
		p.setBillId(bill.getId());
		p.setConsumerId(bill.getConsumerId());
		p.setAmount(bill.getTotalAmount());
		p.setMode(request.getMode());
		p.setStatus(PaymentStatus.SUCCESS);
		p.setProcessedBy("PAYMENT_OFFICER");
		p.setCompletedAt(Instant.now());

		paymentRepo.save(p);
		ConsumerResponse consumer = consumerClient.get(bill.getConsumerId());
		billingClient.markPaid(bill.getId());

		Invoice inv = new Invoice();

		inv.setPaymentId(p.getId());
		inv.setBillId(bill.getId());
		inv.setConsumerId(bill.getConsumerId());
		inv.setMeterNumber(bill.getMeterNumber());
		inv.setUtilityType(bill.getUtilityType());

		inv.setPreviousReading(bill.getPreviousReading());
		inv.setCurrentReading(bill.getCurrentReading());
		inv.setUnitsConsumed(bill.getUnitsConsumed());

		inv.setEnergyCharge(bill.getEnergyCharge());
		inv.setFixedCharge(bill.getFixedCharge());
		inv.setTaxAmount(bill.getTaxAmount());
		inv.setPenaltyAmount(bill.getPenaltyAmount());
		inv.setTotalAmount(bill.getTotalAmount());

		inv.setPaymentMode(p.getMode());
		inv.setPaymentDate(p.getCompletedAt());

		inv.setBillDueDate(bill.getDueDate());
		inv.setBillGeneratedAt(bill.getGeneratedAt());

		invoiceRepo.save(inv);

		InvoicePdfData pdfData = toPdfData(inv);
		byte[] pdf = invoicePdfService.generate(pdfData);
		String base64 = Base64.getEncoder().encodeToString(pdf);

		notificationClient.send(NotificationRequest.builder().email(consumer.getEmail()).type("INVOICE_PDF")
				.subject("Invoice for " + inv.getUtilityType() + " Bill")
				.message("Please find attached your invoice for payment ID: " + inv.getPaymentId())
				.attachmentBase64(base64).attachmentName("invoice-" + inv.getId() + ".pdf").build());

	}

	public Page<Payment> history(String consumerId, int page, int size, String utilityType) {
		PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "completedAt"));

		if (utilityType == null || utilityType.isBlank()) {
			return paymentRepo.findByConsumerId(consumerId, pageable);
		}

		return paymentRepo.findByConsumerIdAndUtilityType(consumerId, utilityType, pageable);
	}

	public List<Invoice> invoices(String consumerId) {
		return invoiceRepo.findByConsumerId(consumerId);
	}

	public Object outstanding(String consumerId) {
		return billingClient.outstanding(consumerId);
	}

	private InvoicePdfData toPdfData(Invoice inv) {
		return InvoicePdfData.builder().invoiceId(inv.getId()).consumerId(inv.getConsumerId())
				.meterNumber(inv.getMeterNumber()).utilityType(inv.getUtilityType())
				.previousReading(inv.getPreviousReading()).currentReading(inv.getCurrentReading())
				.unitsConsumed(inv.getUnitsConsumed()).energyCharge(inv.getEnergyCharge())
				.fixedCharge(inv.getFixedCharge()).taxAmount(inv.getTaxAmount()).penaltyAmount(inv.getPenaltyAmount())
				.totalAmount(inv.getTotalAmount()).billGeneratedAt(inv.getBillGeneratedAt())
				.billDueDate(inv.getBillDueDate()).paymentDate(inv.getPaymentDate()).build();
	}

	public List<Payment> getPayments() {
		return paymentRepo.findAll();
	}

	public byte[] downloadInvoicePdf(String paymentId) {

		Invoice inv = invoiceRepo.findByPaymentId(paymentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
		return invoicePdfService.generate(toPdfData(inv));
	}

	public Page<Payment> getPayments(int page, int size, String search, String mode) {

		PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "completedAt"));

		String s = search == null ? "" : search;
		String m = mode == null || mode.isBlank() ? "" : mode;

		return paymentRepo.search(s, m, pageable);
	}
}
