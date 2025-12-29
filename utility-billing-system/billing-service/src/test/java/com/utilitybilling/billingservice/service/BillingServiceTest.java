package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.dto.GenerateBillRequest;
import com.utilitybilling.billingservice.feign.*;
import com.utilitybilling.billingservice.model.*;
import com.utilitybilling.billingservice.repository.BillRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock MeterClient meterClient;
    @Mock TariffClient tariffClient;
    @Mock ConsumerClient consumerClient;
    @Mock BillRepository billRepo;

    @InjectMocks BillingService service;

    @Test
    void shouldGenerateBillSuccessfully() {
        GenerateBillRequest req = new GenerateBillRequest();
        req.setMeterNumber("m1");

        MeterResponse meter = new MeterResponse();
        meter.setActive(true);
        meter.setConsumerId("c1");
        meter.setUtilityType("ELECTRICITY");

        TariffSlab slab = new TariffSlab();
        slab.setFromUnit(1);
        slab.setToUnit(100);
        slab.setRatePerUnit(5);
        TariffResponse tariff = new TariffResponse();
        tariff.setFixedCharge(50);
        tariff.setTaxPercentage(10);
        tariff.setSlabs(List.of(slab));

        when(meterClient.getMeter("m1")).thenReturn(meter);
        ConsumerExistsResponse exists = new ConsumerExistsResponse();
        exists.setExists(true);
        when(consumerClient.exists("c1")).thenReturn(exists);
        when(meterClient.getLastReading("m1")).thenReturn(150.0);
        when(billRepo.findTopByMeterNumberOrderByGeneratedAtDesc("m1"))
                .thenReturn(Optional.empty());
        when(tariffClient.getActive("ELECTRICITY"))
                .thenReturn(tariff);
        when(billRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = service.generate(req);

        assertEquals("m1", response.getMeterNumber());
        assertEquals(BillStatus.DUE.name(), response.getStatus());
    }

    @Test
    void shouldFailIfMeterInactive() {
        MeterResponse meter = new MeterResponse();
        meter.setActive(false);

        when(meterClient.getMeter("m1")).thenReturn(meter);

        GenerateBillRequest req = new GenerateBillRequest();
        req.setMeterNumber("m1");

        assertThrows(IllegalStateException.class,
                () -> service.generate(req));
    }

    @Test
    void shouldMarkBillPaid() {
        Bill bill = new Bill();
        bill.setStatus(BillStatus.DUE);

        when(billRepo.findById("b1"))
                .thenReturn(Optional.of(bill));

        service.markPaid("b1");

        assertEquals(BillStatus.PAID, bill.getStatus());
        verify(billRepo).save(bill);
    }
}
