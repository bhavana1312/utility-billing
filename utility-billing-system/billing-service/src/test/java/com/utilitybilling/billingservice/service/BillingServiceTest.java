package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.dto.*;
import com.utilitybilling.billingservice.feign.*;
import com.utilitybilling.billingservice.model.*;
import com.utilitybilling.billingservice.repository.BillRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillingServiceTest {

	@Mock
	MeterClient meterClient;
	@Mock
	ConsumerClient consumerClient;
	@Mock
	TariffGateway tariffGateway;
	@Mock
	BillRepository billRepo;
	@Mock
	NotificationClient notificationClient;

	private BillingService service;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		service = new BillingService(meterClient, consumerClient, tariffGateway, billRepo, notificationClient);
	}

	@Test
	void generate_success() {
		GenerateBillRequest req = new GenerateBillRequest();
		req.setMeterNumber("M1");

		MeterResponse meter = new MeterResponse();
		meter.setActive(true);
		meter.setConsumerId("C1");
		meter.setUtilityType("ELECTRICITY");
		meter.setTariffPlan("DOMESTIC");

		ConsumerResponse consumer = ConsumerResponse.builder().email("a@test.com").build();

		TariffSlab slab = new TariffSlab();
		slab.setFromUnit(0);
		slab.setToUnit(100);
		slab.setRatePerUnit(5);

		TariffResponse tariff = new TariffResponse();
		tariff.setSlabs(List.of(slab));
		tariff.setFixedCharge(50);
		tariff.setTaxPercentage(10);

		when(meterClient.getMeter("M1")).thenReturn(meter);
		when(consumerClient.get("C1")).thenReturn(consumer);
		when(meterClient.getLastReading("M1")).thenReturn(120.0);
		when(billRepo.findTopByMeterNumberOrderByGeneratedAtDesc("M1")).thenReturn(Optional.empty());
		when(tariffGateway.getActive("ELECTRICITY", "DOMESTIC")).thenReturn(tariff);
		when(billRepo.save(any())).thenAnswer(i -> {
			Bill b = i.getArgument(0);
			b.setId("B1");
			return b;
		});

		BillResponse res = service.generate(req);

		assertEquals("M1", res.getMeterNumber());
		assertEquals(BillStatus.DUE.name(), res.getStatus());
		verify(notificationClient).send(any());
	}

	@Test
	void consumerBills_success() {
		Bill bill = new Bill();
		bill.setStatus(BillStatus.DUE);

		Page<Bill> page = new PageImpl<>(List.of(bill));

		when(billRepo.findByConsumerIdOrderByGeneratedAtDesc(eq("C1"), any(PageRequest.class))).thenReturn(page);

		Page<BillResponse> res = service.consumerBills("C1", 0, 10);

		assertEquals(1, res.getContent().size());
	}

	@Test
	void all_without_status() {
		Bill bill = new Bill();
		bill.setStatus(BillStatus.DUE);

		Page<Bill> page = new PageImpl<>(List.of(bill));

		when(billRepo.findAll(any(Pageable.class))).thenReturn(page);

		Page<BillResponse> res = service.all(null, 0, 10);

		assertEquals(1, res.getContent().size());
	}

	@Test
	void all_with_status() {
		Bill bill = new Bill();
		bill.setStatus(BillStatus.DUE);

		Page<Bill> page = new PageImpl<>(List.of(bill));

		when(billRepo.findByStatus(eq(BillStatus.DUE), any(Pageable.class))).thenReturn(page);

		Page<BillResponse> res = service.all(BillStatus.DUE, 0, 10);

		assertEquals(1, res.getContent().size());
	}

	@Test
	void getById_success() {
		Bill b = new Bill();
		b.setId("B1");

		when(billRepo.findById("B1")).thenReturn(Optional.of(b));

		assertEquals(b, service.getById("B1"));
	}

	@Test
	void getById_not_found_lambda() {
		when(billRepo.findById("B1")).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> service.getById("B1"));
	}

	@Test
	void markPaid_success() {
		Bill bill = new Bill();
		bill.setStatus(BillStatus.DUE);

		when(billRepo.findById("B1")).thenReturn(Optional.of(bill));

		service.markPaid("B1");

		assertEquals(BillStatus.PAID, bill.getStatus());
	}

	@Test
	void markPaid_not_found_lambda() {
		when(billRepo.findById("B1")).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> service.markPaid("B1"));
	}

	@Test
	void outstanding_success() {
		Bill b1 = new Bill();
		b1.setTotalAmount(BigDecimal.valueOf(100));
		Bill b2 = new Bill();
		b2.setTotalAmount(BigDecimal.valueOf(200));

		when(billRepo.findByConsumerIdAndStatusIn(eq("C1"), any())).thenReturn(List.of(b1, b2));

		OutstandingBalanceResponse r = service.outstanding("C1");

		assertEquals(BigDecimal.valueOf(300), r.getOutstandingAmount());
	}

	@Test
	void generate_inactive_meter() {
		MeterResponse meter = new MeterResponse();
		meter.setActive(false);

		when(meterClient.getMeter("M1")).thenReturn(meter);

		GenerateBillRequest r = new GenerateBillRequest();
		r.setMeterNumber("M1");

		assertThrows(IllegalStateException.class, () -> service.generate(r));
	}

	@Test
	void generate_consumer_null() {
		MeterResponse meter = new MeterResponse();
		meter.setActive(true);
		meter.setConsumerId("C1");

		when(meterClient.getMeter("M1")).thenReturn(meter);
		when(consumerClient.get("C1")).thenReturn(null);

		GenerateBillRequest r = new GenerateBillRequest();
		r.setMeterNumber("M1");

		assertThrows(IllegalArgumentException.class, () -> service.generate(r));
	}

	@Test
	void generate_no_new_consumption() {
		MeterResponse meter = new MeterResponse();
		meter.setActive(true);
		meter.setConsumerId("C1");
		meter.setUtilityType("ELECTRICITY");
		meter.setTariffPlan("DOMESTIC");

		when(meterClient.getMeter("M1")).thenReturn(meter);
		when(consumerClient.get("C1")).thenReturn(ConsumerResponse.builder().email("a@test.com").build());
		when(meterClient.getLastReading("M1")).thenReturn(100.0);

		Bill old = new Bill();
		old.setCurrentReading(100.0);

		when(billRepo.findTopByMeterNumberOrderByGeneratedAtDesc("M1")).thenReturn(Optional.of(old));

		GenerateBillRequest r = new GenerateBillRequest();
		r.setMeterNumber("M1");

		assertThrows(IllegalStateException.class, () -> service.generate(r));
	}

	@Test
	void generate_energy_charge_break_branch() {
		GenerateBillRequest r = new GenerateBillRequest();
		r.setMeterNumber("M1");

		MeterResponse meter = new MeterResponse();
		meter.setActive(true);
		meter.setConsumerId("C1");
		meter.setUtilityType("ELECTRICITY");
		meter.setTariffPlan("DOMESTIC");

		ConsumerResponse consumer = ConsumerResponse.builder().email("a@test.com").build();

		TariffSlab slab1 = new TariffSlab();
		slab1.setFromUnit(0);
		slab1.setToUnit(10);
		slab1.setRatePerUnit(5);

		TariffSlab slab2 = new TariffSlab();
		slab2.setFromUnit(11);
		slab2.setToUnit(100);
		slab2.setRatePerUnit(10);

		TariffResponse tariff = new TariffResponse();
		tariff.setSlabs(List.of(slab1, slab2));
		tariff.setFixedCharge(0);
		tariff.setTaxPercentage(0);

		when(meterClient.getMeter("M1")).thenReturn(meter);
		when(consumerClient.get("C1")).thenReturn(consumer);
		when(meterClient.getLastReading("M1")).thenReturn(10.0);

		Bill old = new Bill();
		old.setCurrentReading(0.0);

		when(billRepo.findTopByMeterNumberOrderByGeneratedAtDesc("M1")).thenReturn(Optional.of(old));

		when(tariffGateway.getActive(any(), any())).thenReturn(tariff);
		when(billRepo.save(any())).thenAnswer(i -> i.getArgument(0));

		service.generate(r);

		verify(billRepo).save(any(Bill.class));
	}
}
