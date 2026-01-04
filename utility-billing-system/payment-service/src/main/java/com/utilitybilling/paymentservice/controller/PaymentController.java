package com.utilitybilling.paymentservice.controller;

import com.utilitybilling.paymentservice.dto.*;
import com.utilitybilling.paymentservice.model.Payment;
import com.utilitybilling.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService service;

	@PostMapping("/initiate")
	public Object initiate(@RequestBody InitiatePaymentRequest r) {
		return service.initiate(r);
	}

	@PostMapping("/confirm")
	public Object confirm(@RequestBody ConfirmPaymentRequest r) {
		return service.confirm(r);
	}

	@PostMapping("/offline")
	public void offline(@RequestBody OfflinePaymentRequest r) {
		service.offlinePay(r);
	}

	@GetMapping("/history/{consumerId}")
	public Page<Payment> history(@PathVariable("consumerId") String consumerId,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "utilityType", required = false) String utilityType) {
		return service.history(consumerId, page, size, utilityType);
	}

	@GetMapping("/invoices/{consumerId}")
	public Object invoices(@PathVariable("consumerId") String consumerId) {
		return service.invoices(consumerId);
	}

	@GetMapping("/outstanding/{consumerId}")
	public Object outstanding(@PathVariable("consumerId") String consumerId) {
		return service.outstanding(consumerId);
	}

	@GetMapping
	public Page<Payment> getPayments(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size,
			@RequestParam(name = "search", required = false) String search,
			@RequestParam(name = "mode", required = false) String mode) {
		return service.getPayments(page, size, search, mode);
	}

	@GetMapping("/{paymentId}/invoice")
	public ResponseEntity<byte[]> downloadInvoice(@PathVariable("paymentId") String paymentId) {

		byte[] pdf = service.downloadInvoicePdf(paymentId);

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + paymentId + ".pdf")
				.body(pdf);
	}
}
