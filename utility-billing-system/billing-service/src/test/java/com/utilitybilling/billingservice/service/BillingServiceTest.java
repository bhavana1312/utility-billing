package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.dto.*;
import com.utilitybilling.billingservice.feign.*;
import com.utilitybilling.billingservice.model.*;
import com.utilitybilling.billingservice.repository.BillRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillingServiceTest {

	@Mock MeterClient meterClient;
	@Mock ConsumerClient consumerClient;
	@Mock TariffClient tariffClient;
	@Mock BillRepository billRepo;
	@Mock NotificationClient notificationClient;

	private BillingService service;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		service = new BillingService(
				meterClient,
				consumerClient,
				tariffClient,
				billRepo,
				notificationClient
		);
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
		when(billRepo.findTopByMeterNumberOrderByGeneratedAtDesc("M1"))
				.thenReturn(Optional.empty());
		when(tariffClient.getActive("ELECTRICITY","DOMESTIC")).thenReturn(tariff);
		when(billRepo.save(any())).thenAnswer(i -> {
			Bill b=i.getArgument(0);
			b.setId("B1");
			return b;
		});

		BillResponse res=service.generate(req);

		assertEquals("M1",res.getMeterNumber());
		assertEquals(BillStatus.DUE.name(),res.getStatus());
		verify(notificationClient).send(any());
	}

	@Test
	void consumerBills_success() {

		Bill bill=new Bill();
		bill.setStatus(BillStatus.DUE);

		Page<Bill> page=new PageImpl<>(List.of(bill));

		when(billRepo.findByConsumerIdOrderByGeneratedAtDesc(eq("C1"),any(PageRequest.class)))
				.thenReturn(page);

		Page<BillResponse> res=service.consumerBills("C1",0,10);

		assertEquals(1,res.getContent().size());
	}

	@Test
	void all_without_status() {

		Bill bill=new Bill();
		bill.setStatus(BillStatus.DUE);

		Page<Bill> page=new PageImpl<>(List.of(bill));

		when(billRepo.findAll(any(Pageable.class))).thenReturn(page);

		Page<BillResponse> res=service.all(null,0,10);

		assertEquals(1,res.getContent().size());
	}

	@Test
	void all_with_status() {

		Bill bill=new Bill();
		bill.setStatus(BillStatus.DUE);

		Page<Bill> page=new PageImpl<>(List.of(bill));

		when(billRepo.findByStatus(eq(BillStatus.DUE),any(Pageable.class)))
				.thenReturn(page);

		Page<BillResponse> res=service.all(BillStatus.DUE,0,10);

		assertEquals(1,res.getContent().size());
	}
}
