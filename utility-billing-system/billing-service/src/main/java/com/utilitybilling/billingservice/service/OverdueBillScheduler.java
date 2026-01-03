package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.feign.ConsumerClient;
import com.utilitybilling.billingservice.feign.ConsumerResponse;
import com.utilitybilling.billingservice.feign.NotificationClient;
import com.utilitybilling.billingservice.feign.NotificationRequest;
import com.utilitybilling.billingservice.feign.TariffClient;
import com.utilitybilling.billingservice.model.*;
import com.utilitybilling.billingservice.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OverdueBillScheduler {

	private final BillRepository billRepo;
	private final TariffClient tariffClient;
	private final NotificationClient notificationClient;
	private final ConsumerClient consumerClient;

	@Scheduled(cron = "0 30 09 * * *")
	public void markOverdueBills() {

		List<Bill> dueBills = new ArrayList<>();

		dueBills.addAll(billRepo.findByStatusAndDueDateBefore(BillStatus.DUE, Instant.now()));
		dueBills.addAll(billRepo.findByStatusAndDueDateBefore(BillStatus.OVERDUE, Instant.now()));

		for (Bill bill : dueBills) {

			int overdueDays = (int) ChronoUnit.DAYS.between(bill.getDueDate().toInstant(), Instant.now());

			var tariff = tariffClient.getActive(bill.getUtilityType(), bill.getTariffPlan());

			BigDecimal penalty = PenaltyCalculator.calculatePenalty(bill.getTotalAmount(), overdueDays,
					tariff.getOverduePenaltySlabs());
			
			ConsumerResponse consumer = consumerClient.get(bill.getConsumerId());

			bill.setPenaltyAmount(penalty);
			bill.setTotalAmount(bill.getTotalAmount().add(penalty));
			bill.setStatus(BillStatus.OVERDUE);
			bill.setLastUpdatedAt(Instant.now());

			notificationClient.send(NotificationRequest.builder().email(consumer.getEmail()).type("BILL_OVERDUE")
					.subject("Your utility bill is overdue")
					.message("Your " + bill.getUtilityType() + " bill is overdue.\n\n" + "Bill ID: " + bill.getId()
							+ "\n" + "Amount Due: ₹" + bill.getTotalAmount() + "\n" + "Penalty Amount: ₹"
							+ bill.getPenaltyAmount() + "\n" + "Due Date: " + bill.getDueDate())
					.build());

			billRepo.save(bill);
		}
	}
}
