package com.utilitybilling.paymentservice.service;

import com.utilitybilling.paymentservice.client.*;
import com.utilitybilling.paymentservice.dto.*;
import com.utilitybilling.paymentservice.model.*;
import com.utilitybilling.paymentservice.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	@Mock
	private PaymentRepository paymentRepo;

	@Mock
	private InvoiceRepository invoiceRepo;

	@Mock
	private BillingClient billingClient;

	@InjectMocks
	private PaymentService service;

	@Test
	void shouldInitiatePaymentSuccessfully() {
		InitiatePaymentRequest req = new InitiatePaymentRequest();
		req.setBillId("B1");

		BillResponse bill = new BillResponse();
		bill.setId("B1");
		bill.setConsumerId("C1");
		bill.setStatus(BillStatus.DUE);
		bill.setTotalAmount(500.0);

		when(billingClient.getBill("B1")).thenReturn(bill);
		when(paymentRepo.findByBillIdAndStatus("B1", PaymentStatus.SUCCESS)).thenReturn(null);

		Object result = service.initiate(req);

		assertTrue(result instanceof Payment);
		Payment p = (Payment) result;

		assertEquals("B1", p.getBillId());
		assertEquals(PaymentStatus.INITIATED, p.getStatus());
		assertNotNull(p.getOtp());

		verify(paymentRepo).save(any(Payment.class));
	}

	@Test
	void shouldFailInitiateWhenBillNotPayable() {
		InitiatePaymentRequest req = new InitiatePaymentRequest();
		req.setBillId("B1");

		BillResponse bill = new BillResponse();
		bill.setStatus(BillStatus.PAID);

		when(billingClient.getBill("B1")).thenReturn(bill);

		assertThrows(IllegalStateException.class, () -> service.initiate(req));
	}

	@Test
	void shouldConfirmPaymentSuccessfully() {
		ConfirmPaymentRequest req = new ConfirmPaymentRequest();
		req.setPaymentId("P1");
		req.setOtp("123456");

		Payment p = new Payment();
		p.setId("P1");
		p.setBillId("B1");
		p.setConsumerId("C1");
		p.setAmount(300.0);
		p.setMode(PaymentMode.ONLINE);
		p.setStatus(PaymentStatus.INITIATED);
		p.setOtp("123456");
		p.setOtpExpiresAt(Instant.now().plusSeconds(60));

		when(paymentRepo.findById("P1")).thenReturn(Optional.of(p));

		Object result = service.confirm(req);

		assertTrue(result instanceof Invoice);

		verify(billingClient).markPaid("B1");
		verify(paymentRepo, atLeastOnce()).save(p);
		verify(invoiceRepo).save(any(Invoice.class));
	}

	@Test
	void shouldFailConfirmWithInvalidOtp() {
		ConfirmPaymentRequest req = new ConfirmPaymentRequest();
		req.setPaymentId("P1");
		req.setOtp("000000");

		Payment p = new Payment();
		p.setStatus(PaymentStatus.INITIATED);
		p.setOtp("123456");
		p.setOtpExpiresAt(Instant.now().plusSeconds(60));

		when(paymentRepo.findById("P1")).thenReturn(Optional.of(p));

		assertThrows(IllegalArgumentException.class, () -> service.confirm(req));

		verify(paymentRepo).save(p);
	}

	@Test
	void shouldProcessOfflinePayment() {
		OfflinePaymentRequest req = new OfflinePaymentRequest();
		req.setBillId("B1");
		req.setMode(PaymentMode.CASH);

		BillResponse bill = new BillResponse();
		bill.setId("B1");
		bill.setConsumerId("C1");
		bill.setTotalAmount(200.0);
		bill.setStatus(BillStatus.DUE);

		when(billingClient.getBill("B1")).thenReturn(bill);

		service.offlinePay(req);

		verify(paymentRepo).save(any(Payment.class));
		verify(invoiceRepo).save(any(Invoice.class));
		verify(billingClient).markPaid("B1");
	}

	@Test
	void shouldReturnPaymentHistory() {
		when(paymentRepo.findByConsumerId("C1")).thenReturn(List.of(new Payment()));

		assertEquals(1, service.history("C1").size());
	}

	@Test
	void shouldReturnInvoices() {
		when(invoiceRepo.findByConsumerId("C1")).thenReturn(List.of(new Invoice()));

		assertEquals(1, service.invoices("C1").size());
	}
}
