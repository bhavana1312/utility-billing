package com.utilitybilling.meterservice.config;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.utilitybilling.meterservice.model.MeterReading;
import com.utilitybilling.meterservice.repository.MeterReadingRepository;
import com.utilitybilling.meterservice.repository.MeterRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MeterReadingSeedDataInitializer {

	private final MeterRepository meterRepo;
	private final MeterReadingRepository readingRepo;

	@Bean
	public CommandLineRunner seedMeterReadings() {
		return args -> {

			if (readingRepo.count() > 0)
				return;

			Random random = new Random();
			Instant start = Instant.parse("2025-02-01T00:00:00Z");

			List<MeterReading> allReadings = new ArrayList<>();

			meterRepo.findAll().forEach(meter -> {

				double base = 100 + random.nextInt(300);
				double latest = base;

				for (int i = 0; i < 6; i++) {
					latest += 20 + random.nextInt(40);

					MeterReading r = new MeterReading();
					r.setMeterNumber(meter.getMeterNumber());
					r.setReadingValue(latest);
					r.setReadingDate(start.plus(i * 30, ChronoUnit.DAYS));
					allReadings.add(r);
				}

				meter.setLastReading(latest);
				meterRepo.save(meter);
			});

			readingRepo.saveAll(allReadings);
		};
	}
}
