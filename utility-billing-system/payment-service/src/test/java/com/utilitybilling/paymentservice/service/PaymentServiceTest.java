package com.utilitybilling.paymentservice.service;

import com.utilitybilling.paymentservice.dto.*;
import com.utilitybilling.paymentservice.feign.*;
import com.utilitybilling.paymentservice.model.*;
import com.utilitybilling.paymentservice.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

	@Mock
	PaymentRepository paymentRepo;
	@Mock
	InvoiceRepository invoiceRepo;
	@Mock
	BillingClient billingClient;
	@Mock
	ConsumerClient consumerClient;
	@Mock
	NotificationClient notificationClient;
	@Mock
	InvoicePdfService invoicePdfService;

	private PaymentService service;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		service = new PaymentService(paymentRepo, invoiceRepo, billingClient, consumerClient, notificationClient,
				invoicePdfService);
	}

	@Test
	void initiate_success() {
		BillResponse bill = new BillResponse();
		bill.setId("B1");
		bill.setStatus(BillStatus.DUE);
		bill.setConsumerId("C1");
		bill.setUtilityType("ELECTRICITY");
		bill.setTotalAmount(BigDecimal.valueOf(100));

		ConsumerResponse consumer = mock(ConsumerResponse.class);
		when(consumer.getEmail()).thenReturn("e@test.com");

		when(billingClient.getBill("B1")).thenReturn(bill);
		when(consumerClient.get("C1")).thenReturn(consumer);

		InitiatePaymentRequest r = new InitiatePaymentRequest();
		r.setBillId("B1");

		assertNotNull(service.initiate(r));
	}

	@Test
	void initiate_not_payable() {
		BillResponse bill = new BillResponse();
		bill.setStatus(BillStatus.PAID);

		when(billingClient.getBill("B1")).thenReturn(bill);

		InitiatePaymentRequest r = new InitiatePaymentRequest();
		r.setBillId("B1");

		assertThrows(IllegalStateException.class, () -> service.initiate(r));
	}

	@Test
	void confirm_invalid_otp() {
		Payment p = new Payment();
		p.setId("P1");
		p.setStatus(PaymentStatus.INITIATED);
		p.setOtp("111");
		p.setOtpExpiresAt(Instant.now().minusSeconds(1));
		p.setConsumerId("C1");
		p.setUtilityType("E");

		ConsumerResponse consumer = mock(ConsumerResponse.class);
		when(consumer.getEmail()).thenReturn("e@test.com");

		when(paymentRepo.findById("P1")).thenReturn(Optional.of(p));
		when(consumerClient.get("C1")).thenReturn(consumer);

		ConfirmPaymentRequest r = new ConfirmPaymentRequest();
		r.setPaymentId("P1");
		r.setOtp("000");

		assertThrows(IllegalArgumentException.class, () -> service.confirm(r));
	}

	@Test
	void confirm_success() {
		Payment p = new Payment();
		p.setId("P1");
		p.setStatus(PaymentStatus.INITIATED);
		p.setOtp("123");
		p.setOtpExpiresAt(Instant.now().plusSeconds(60));
		p.setConsumerId("C1");
		p.setUtilityType("ELECTRICITY");
		p.setBillId("B1");

		BillResponse bill = new BillResponse();
		bill.setId("B1");
		bill.setConsumerId("C1");
		bill.setUtilityType("ELECTRICITY");
		bill.setMeterNumber("M1");
		bill.setPreviousReading(0);
		bill.setCurrentReading(10);
		bill.setUnitsConsumed(10);
		bill.setEnergyCharge(BigDecimal.TEN);
		bill.setFixedCharge(BigDecimal.ZERO);
		bill.setTaxAmount(BigDecimal.ZERO);
		bill.setPenaltyAmount(BigDecimal.ZERO);
		bill.setTotalAmount(BigDecimal.TEN);
		bill.setGeneratedAt(Instant.now());
		bill.setDueDate(Instant.now());

		ConsumerResponse consumer = mock(ConsumerResponse.class);
		when(consumer.getEmail()).thenReturn("e@test.com");

		when(paymentRepo.findById("P1")).thenReturn(Optional.of(p));
		when(billingClient.getBill("B1")).thenReturn(bill);
		when(consumerClient.get("C1")).thenReturn(consumer);
		when(invoicePdfService.generate(any())).thenReturn(new byte[] { 1 });

		ConfirmPaymentRequest r = new ConfirmPaymentRequest();
		r.setPaymentId("P1");
		r.setOtp("123");

		assertNotNull(service.confirm(r));
	}

	@Test
	void offlinePay_success() {
		BillResponse bill = new BillResponse();
		bill.setId("B1");
		bill.setStatus(BillStatus.DUE);
		bill.setConsumerId("C1");
		bill.setUtilityType("ELECTRICITY");
		bill.setMeterNumber("M1");
		bill.setTotalAmount(BigDecimal.valueOf(50));
		bill.setGeneratedAt(Instant.now());
		bill.setDueDate(Instant.now());

		ConsumerResponse consumer = mock(ConsumerResponse.class);
		when(consumer.getEmail()).thenReturn("e@test.com");

		when(billingClient.getBill("B1")).thenReturn(bill);
		when(consumerClient.get("C1")).thenReturn(consumer);
		when(invoicePdfService.generate(any())).thenReturn(new byte[] { 1 });

		OfflinePaymentRequest r = new OfflinePaymentRequest();
		r.setBillId("B1");
		r.setMode(PaymentMode.CASH);

		service.offlinePay(r);

		verify(billingClient).markPaid("B1");
	}

	@Test
	void history_with_utility() {
		Page<Payment> page = new PageImpl<>(List.of(new Payment()));

		when(paymentRepo.findByConsumerIdAndUtilityType(eq("C1"), eq("E"), any())).thenReturn(page);

		assertEquals(1, service.history("C1", 0, 10, "E").getContent().size());
	}

	@Test
	void history_without_utility() {
		Page<Payment> page = new PageImpl<>(List.of(new Payment()));

		when(paymentRepo.findByConsumerId(eq("C1"), any())).thenReturn(page);

		assertEquals(1, service.history("C1", 0, 10, null).getContent().size());
	}

	@Test
	void downloadInvoice_not_found() {
		when(invoiceRepo.findByPaymentId("P1")).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> service.downloadInvoicePdf("P1"));
	}

	@Test
	void getPayments_search() {
		Page<Payment> page = new PageImpl<>(List.of(new Payment()));

		when(paymentRepo.search(any(), any(), any())).thenReturn(page);

		assertEquals(1, service.getPayments(0, 10, "x", "y").getContent().size());
	}
}
