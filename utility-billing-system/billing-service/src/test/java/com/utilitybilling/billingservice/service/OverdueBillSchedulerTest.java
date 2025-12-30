//package com.utilitybilling.billingservice.service;
//
//import com.utilitybilling.billingservice.feign.*;
//import com.utilitybilling.billingservice.model.*;
//import com.utilitybilling.billingservice.repository.BillRepository;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.Instant;
//import java.util.List;
//
//import static org.mockito.Mockito.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//
//@ExtendWith(MockitoExtension.class)
//class OverdueBillSchedulerTest {
//
//    @Mock BillRepository billRepo;
//    @Mock TariffClient tariffClient;
//
//    @InjectMocks OverdueBillScheduler scheduler;
//
//    @Test
//    void shouldMarkBillsOverdue() {
//        Bill bill = new Bill();
//        bill.setDueDate(Instant.now().minusSeconds(86400));
//        bill.setTotalAmount(100);
//        bill.setUtilityType("ELECTRICITY");
//        bill.setStatus(BillStatus.DUE);
//
//        OverduePenaltySlab slab = new OverduePenaltySlab();
//        slab.setFromDay(1);
//        slab.setToDay(10);
//        slab.setPenaltyPercentage(10);
//
//        TariffResponse tariff = new TariffResponse();
//        tariff.setOverduePenaltySlabs(List.of(slab));
//
//        when(billRepo.findByStatusAndDueDateBefore(
//                eq(BillStatus.DUE), any()))
//                .thenReturn(List.of(bill));
//        when(tariffClient.getActive("ELECTRICITY"))
//                .thenReturn(tariff);
//
//        scheduler.markOverdueBills();
//
//        verify(billRepo).save(bill);
//        assert bill.getStatus() == BillStatus.OVERDUE;
//    }
//}
