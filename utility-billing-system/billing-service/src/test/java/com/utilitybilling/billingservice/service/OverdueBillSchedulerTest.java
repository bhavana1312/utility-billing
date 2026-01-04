package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.feign.*;
import com.utilitybilling.billingservice.model.*;
import com.utilitybilling.billingservice.repository.BillRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

class OverdueBillSchedulerTest {

	@Mock
	BillRepository billRepo;
	@Mock
	TariffClient tariffClient;
	@Mock
	NotificationClient notificationClient;
	@Mock
	ConsumerClient consumerClient;

	private OverdueBillScheduler scheduler;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		scheduler = new OverdueBillScheduler(billRepo, tariffClient, notificationClient, consumerClient);
	}

	@Test
	void markOverdueBills_success() {
		Bill bill = new Bill();
		bill.setId("B1");
		bill.setUtilityType("ELECTRICITY");
		bill.setTariffPlan("DOMESTIC");
		bill.setTotalAmount(BigDecimal.valueOf(100));
		bill.setDueDate(Date.from(Instant.now().minusSeconds(86400)));
		bill.setStatus(BillStatus.DUE);

		OverduePenaltySlab slab = new OverduePenaltySlab();
		slab.setFromDay(1);
		slab.setToDay(10);
		slab.setPenaltyPercentage(5);

		TariffResponse tariff = new TariffResponse();
		tariff.setOverduePenaltySlabs(List.of(slab));

		ConsumerResponse consumer = ConsumerResponse.builder().email("x@test.com").build();

		when(billRepo.findByStatusAndDueDateBefore(eq(BillStatus.DUE), any())).thenReturn(List.of(bill));
		when(billRepo.findByStatusAndDueDateBefore(eq(BillStatus.OVERDUE), any())).thenReturn(List.of());
		when(tariffClient.getActive(any(), any())).thenReturn(tariff);
		when(consumerClient.get(any())).thenReturn(consumer);

		scheduler.markOverdueBills();

		verify(notificationClient).send(any());
		verify(billRepo).save(bill);
	}
}
