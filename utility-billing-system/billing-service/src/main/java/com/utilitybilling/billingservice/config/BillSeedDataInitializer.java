package com.utilitybilling.billingservice.config;

import com.utilitybilling.billingservice.feign.ConsumerClient;
import com.utilitybilling.billingservice.feign.MeterClient;
import com.utilitybilling.billingservice.feign.TariffClient;
import com.utilitybilling.billingservice.feign.TariffResponse;
import com.utilitybilling.billingservice.model.Bill;
import com.utilitybilling.billingservice.model.BillStatus;
import com.utilitybilling.billingservice.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Configuration
@RequiredArgsConstructor
public class BillSeedDataInitializer {

	private final BillRepository billRepo;
	private final MeterClient meterClient;
	private final ConsumerClient consumerClient;
	private final TariffClient tariffClient;

	public void seedBills() {

		if (billRepo.count() > 0)
			return;

		try {
			var meters = meterClient.getAllMeters();
			if (meters.isEmpty())
				return;

			for (var meter : meters) {

				var consumer = consumerClient.get(meter.getConsumerId());
				if (consumer == null)
					continue;

				double previous = 0;

				for (int i = 0; i < 4; i++) {

					double current = previous + 80 + (i * 30);

					TariffResponse tariff = tariffClient.getActive(meter.getUtilityType(), meter.getTariffPlan());

					BigDecimal energyCharge = calculateEnergyCharge(current - previous, tariff);

					BigDecimal fixedCharge = BigDecimal.valueOf(tariff.getFixedCharge());

					BigDecimal taxAmount = energyCharge.add(fixedCharge)
							.multiply(BigDecimal.valueOf(tariff.getTaxPercentage()))
							.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

					BigDecimal total = energyCharge.add(fixedCharge).add(taxAmount).setScale(2, RoundingMode.HALF_UP);

					Bill bill = new Bill();
					bill.setConsumerId(meter.getConsumerId());
					bill.setMeterNumber(meter.getMeterNumber());
					bill.setUtilityType(meter.getUtilityType());
					bill.setTariffPlan(meter.getTariffPlan());
					bill.setPreviousReading(previous);
					bill.setCurrentReading(current);
					bill.setUnitsConsumed(current - previous);
					bill.setEnergyCharge(energyCharge);
					bill.setFixedCharge(fixedCharge);
					bill.setTaxAmount(taxAmount);
					bill.setPenaltyAmount(BigDecimal.ZERO);
					bill.setTotalAmount(total);
					bill.setGeneratedAt(Instant.now().minus(90 - i * 20, ChronoUnit.DAYS));
					bill.setDueDate(Date.from(bill.getGeneratedAt().plus(7, ChronoUnit.DAYS)));
					bill.setLastUpdatedAt(Instant.now());
					bill.setStatus(BillStatus.DUE);

					billRepo.save(bill);

					previous = current;
				}
			}
		} catch (Exception ignored) {
			System.out.println(ignored);
		}
	}

	private BigDecimal calculateEnergyCharge(double units, TariffResponse tariff) {
		BigDecimal total = BigDecimal.ZERO;
		double remaining = units;

		for (var slab : tariff.getSlabs()) {
			if (remaining <= 0)
				break;

			double range = slab.getToUnit() - slab.getFromUnit() + 1;
			double used = Math.min(remaining, range);

			total = total.add(BigDecimal.valueOf(used).multiply(BigDecimal.valueOf(slab.getRatePerUnit())));

			remaining -= used;
		}

		return total.setScale(2, RoundingMode.HALF_UP);
	}
}
