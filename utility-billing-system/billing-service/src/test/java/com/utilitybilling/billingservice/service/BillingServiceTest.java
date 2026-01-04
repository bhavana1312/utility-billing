package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.dto.*;
import com.utilitybilling.billingservice.feign.*;
import com.utilitybilling.billingservice.model.*;
import com.utilitybilling.billingservice.repository.BillRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillingServiceTest {

    @Mock MeterClient meterClient;
    @Mock ConsumerClient consumerClient;
    @Mock TariffGateway tariffGateway;
    @Mock BillRepository billRepo;
    @Mock NotificationClient notificationClient;

    private BillingService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service=new BillingService(
                meterClient,
                consumerClient,
                tariffGateway,
                billRepo,
                notificationClient
        );
    }

    @Test
    void generate_success() {
        GenerateBillRequest req=new GenerateBillRequest();
        req.setMeterNumber("M1");

        MeterResponse meter=new MeterResponse();
        meter.setActive(true);
        meter.setConsumerId("C1");
        meter.setUtilityType("ELECTRICITY");
        meter.setTariffPlan("DOMESTIC");

        ConsumerResponse consumer=ConsumerResponse.builder()
                .email("a@test.com")
                .build();

        TariffSlab slab=new TariffSlab();
        slab.setFromUnit(0);
        slab.setToUnit(100);
        slab.setRatePerUnit(5);

        TariffResponse tariff=new TariffResponse();
        tariff.setSlabs(List.of(slab));
        tariff.setFixedCharge(50);
        tariff.setTaxPercentage(10);

        when(meterClient.getMeter("M1")).thenReturn(meter);
        when(consumerClient.get("C1")).thenReturn(consumer);
        when(meterClient.getLastReading("M1")).thenReturn(120.0);
        when(billRepo.findTopByMeterNumberOrderByGeneratedAtDesc("M1"))
                .thenReturn(Optional.empty());
        when(tariffGateway.getActive("ELECTRICITY","DOMESTIC"))
                .thenReturn(tariff);
        when(billRepo.save(any())).thenAnswer(i->{
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
    void markPaid_success() {
        Bill bill=new Bill();
        bill.setStatus(BillStatus.DUE);

        when(billRepo.findById("B1")).thenReturn(Optional.of(bill));

        service.markPaid("B1");

        assertEquals(BillStatus.PAID,bill.getStatus());
        verify(billRepo).save(bill);
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
