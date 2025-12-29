package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.feign.TariffClient;
import com.utilitybilling.billingservice.model.*;
import com.utilitybilling.billingservice.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OverdueBillScheduler{

    private final BillRepository billRepo;
    private final TariffClient tariffClient;

    @Scheduled(cron="0 0 1 * * *")
    public void markOverdueBills(){

        List<Bill> dueBills=
                billRepo.findByStatusAndDueDateBefore(
                        BillStatus.DUE,Instant.now());

        for(Bill bill:dueBills){

            int overdueDays=(int)ChronoUnit.DAYS.between(
                    bill.getDueDate(),Instant.now());

            var tariff=tariffClient.getActive(bill.getUtilityType());

            double penalty=PenaltyCalculator.calculatePenalty(
                    bill.getTotalAmount(),
                    overdueDays,
                    tariff.getOverduePenaltySlabs()
            );

            bill.setPenaltyAmount(penalty);
            bill.setTotalAmount(bill.getTotalAmount()+penalty);
            bill.setStatus(BillStatus.OVERDUE);
            bill.setLastUpdatedAt(Instant.now());

            billRepo.save(bill);
        }
    }
}
