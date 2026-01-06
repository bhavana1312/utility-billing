package com.utilitybilling.meterservice.config;

import com.utilitybilling.meterservice.feign.ConsumerClient;
import com.utilitybilling.meterservice.model.ConnectionRequest;
import com.utilitybilling.meterservice.model.ConnectionStatus;
import com.utilitybilling.meterservice.model.TariffPlan;
import com.utilitybilling.meterservice.model.UtilityType;
import com.utilitybilling.meterservice.repository.ConnectionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ConnectionRequestSeedDataInitializer {

	private final ConnectionRequestRepository repo;
	private final ConsumerClient consumerClient;

	@Bean
	public CommandLineRunner seedConnectionRequests() {
		return args -> {

			if (repo.count() > 0)
				return;

			Instant base = Instant.parse("2025-01-20T08:00:00Z");

			seedMultiple("Aarav Sharma", List.of(req(UtilityType.ELECTRICITY, TariffPlan.DOMESTIC, base),
					req(UtilityType.WATER, TariffPlan.DOMESTIC, base.plusSeconds(1800))));

			seedMultiple("Ananya Reddy",
					List.of(req(UtilityType.ELECTRICITY, TariffPlan.DOMESTIC, base.plusSeconds(3600)),
							req(UtilityType.GAS, TariffPlan.DOMESTIC, base.plusSeconds(5400))));

			seedMultiple("Rohit Verma",
					List.of(req(UtilityType.ELECTRICITY, TariffPlan.COMMERCIAL, base.plusSeconds(7200)),
							req(UtilityType.WATER, TariffPlan.COMMERCIAL, base.plusSeconds(9000))));

			seedMultiple("Sneha Iyer", List.of(req(UtilityType.GAS, TariffPlan.DOMESTIC, base.plusSeconds(10800)),
					req(UtilityType.ELECTRICITY, TariffPlan.DOMESTIC, base.plusSeconds(12600))));

			seedMultiple("Kunal Mehta",
					List.of(req(UtilityType.ELECTRICITY, TariffPlan.DOMESTIC, base.plusSeconds(14400)),
							req(UtilityType.WATER, TariffPlan.DOMESTIC, base.plusSeconds(16200))));

			seedMultiple("Neha Kapoor", List.of(req(UtilityType.WATER, TariffPlan.COMMERCIAL, base.plusSeconds(18000)),
					req(UtilityType.GAS, TariffPlan.COMMERCIAL, base.plusSeconds(19800))));

			seedMultiple("Aditya Singh", List.of(req(UtilityType.GAS, TariffPlan.DOMESTIC, base.plusSeconds(21600)),
					req(UtilityType.ELECTRICITY, TariffPlan.DOMESTIC, base.plusSeconds(23400))));

			seedMultiple("Pooja Malhotra",
					List.of(req(UtilityType.ELECTRICITY, TariffPlan.INDUSTRIAL, base.plusSeconds(25200)),
							req(UtilityType.WATER, TariffPlan.INDUSTRIAL, base.plusSeconds(27000))));

			seedMultiple("Vikram Joshi", List.of(req(UtilityType.WATER, TariffPlan.DOMESTIC, base.plusSeconds(28800)),
					req(UtilityType.ELECTRICITY, TariffPlan.DOMESTIC, base.plusSeconds(30600))));

			seedMultiple("Riya Chatterjee",
					List.of(req(UtilityType.ELECTRICITY, TariffPlan.DOMESTIC, base.plusSeconds(32400)),
							req(UtilityType.GAS, TariffPlan.DOMESTIC, base.plusSeconds(34200))));

			repo.saveAll(List.of(rejected("INVALID_CONSUMER", "No consumer found", base.plusSeconds(36000)),
					rejected("DUPLICATE", "Duplicate connection request", base.plusSeconds(37800)),
					rejected("LIMIT_EXCEEDED", "Maximum connections exceeded", base.plusSeconds(39600))));
		};
	}

	private void seedMultiple(String username, List<ConnectionRequest> requests) {
		try {
			var consumer = consumerClient.getByUsername(username);
			requests.forEach(r -> {
				r.setConsumerId(consumer.getId());
				repo.save(r);
			});
		} catch (Exception ignored) {
		}
	}

	private ConnectionRequest req(UtilityType utility, TariffPlan plan, Instant time) {
		ConnectionRequest r = new ConnectionRequest();
		r.setUtilityType(utility);
		r.setTariffPlan(plan);
		r.setCreatedAt(time);
		r.setStatus(ConnectionStatus.PENDING);
		return r;
	}

	private ConnectionRequest rejected(String consumerId, String reason, Instant time) {
		ConnectionRequest r = new ConnectionRequest();
		r.setConsumerId(consumerId);
		r.setUtilityType(UtilityType.ELECTRICITY);
		r.setTariffPlan(TariffPlan.DOMESTIC);
		r.setStatus(ConnectionStatus.REJECTED);
		r.setRejectionReason(reason);
		r.setCreatedAt(time);
		return r;
	}
}
