package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.dto.OutstandingBalanceResponse;
import com.utilitybilling.billingservice.model.*;
import com.utilitybilling.billingservice.repository.BillRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InternalBillingServiceTest {

	@Mock BillRepository billRepo;

	private InternalBillingService service;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		service=new InternalBillingService(billRepo);
	}

	@Test
	void getById_success() {

		Bill bill=new Bill();
		bill.setId("B1");

		when(billRepo.findById("B1")).thenReturn(Optional.of(bill));

		assertEquals("B1",service.getById("B1").getBillId());
	}

	@Test
	void getById_not_found() {
		when(billRepo.findById("B1")).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class,()->service.getById("B1"));
	}

	@Test
	void markPaid_success() {

		Bill bill=new Bill();
		bill.setStatus(BillStatus.DUE);

		when(billRepo.findById("B1")).thenReturn(Optional.of(bill));

		service.markPaid("B1");

		assertEquals(BillStatus.PAID,bill.getStatus());
	}

	@Test
	void markPaid_not_found() {
		when(billRepo.findById("B1")).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class,()->service.markPaid("B1"));
	}

	@Test
	void outstanding_success() {

		Bill b1=new Bill();
		b1.setTotalAmount(BigDecimal.valueOf(100));

		Bill b2=new Bill();
		b2.setTotalAmount(BigDecimal.valueOf(200));

		when(billRepo.findByConsumerIdAndStatusIn(eq("C1"),any()))
				.thenReturn(List.of(b1,b2));

		OutstandingBalanceResponse r=service.outstanding("C1");

		assertEquals(BigDecimal.valueOf(300),r.getOutstandingAmount());
	}
}
