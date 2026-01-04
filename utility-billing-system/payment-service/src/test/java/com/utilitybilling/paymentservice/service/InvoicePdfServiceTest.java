package com.utilitybilling.paymentservice.service;

import com.utilitybilling.paymentservice.dto.InvoicePdfData;
import com.utilitybilling.paymentservice.feign.ConsumerClient;
import com.utilitybilling.paymentservice.feign.ConsumerResponse;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoicePdfServiceTest {

	@Mock
	ConsumerClient consumerClient;

	private InvoicePdfService service;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		service = new InvoicePdfService(consumerClient);
	}

	@Test
	void generate_success() {
		when(consumerClient.get("C1")).thenReturn(ConsumerResponse.builder().email("e").build());

		InvoicePdfData d = InvoicePdfData.builder().invoiceId("I1").consumerId("C1").utilityType("E").meterNumber("M1")
				.previousReading(0).currentReading(10).unitsConsumed(10).energyCharge(BigDecimal.TEN)
				.fixedCharge(BigDecimal.ZERO).taxAmount(BigDecimal.ZERO).penaltyAmount(BigDecimal.ZERO)
				.totalAmount(BigDecimal.TEN).billGeneratedAt(Instant.now()).billDueDate(Instant.now())
				.paymentDate(Instant.now()).build();

		byte[] pdf = service.generate(d);

		assertNotNull(pdf);
		assertTrue(pdf.length > 0);
	}
}
