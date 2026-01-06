package com.utilitybilling.consumerservice.config;

import com.utilitybilling.consumerservice.model.ConsumerRequest;
import com.utilitybilling.consumerservice.repository.ConsumerRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ConsumerRequestSeedDataInitializer {

	private final ConsumerRequestRepository requestRepo;

	@Bean
	public CommandLineRunner seedConsumerRequests() {
		return args -> {

			if (requestRepo.count() > 0)
				return;

			Instant base = Instant.parse("2025-01-05T08:00:00Z");

			requestRepo.saveAll(List.of(

					pending("Aarav Sharma", "aarav.sharma@gmail.com", "9876543210", "Flat 12B, Green Residency", base),
					pending("Ananya Reddy", "ananya.reddy@gmail.com", "9876543211", "Plot 45, Lake View Homes",
							base.plusSeconds(3600)),
					pending("Rohit Verma", "rohit.verma@gmail.com", "9876543212", "House 7, Sunrise Colony",
							base.plusSeconds(7200)),
					pending("Sneha Iyer", "sneha.iyer@gmail.com", "9876543213", "Flat 303, Elite Towers",
							base.plusSeconds(10800)),
					pending("Kunal Mehta", "kunal.mehta@gmail.com", "9876543214", "Villa 19, Palm Meadows",
							base.plusSeconds(14400)),
					pending("Neha Kapoor", "neha.kapoor@gmail.com", "9876543215", "Flat 88, Sky Heights",
							base.plusSeconds(18000)),
					pending("Aditya Singh", "aditya.singh@gmail.com", "9876543216", "House 21, Rose Garden",
							base.plusSeconds(21600)),
					pending("Pooja Malhotra", "pooja.m@gmail.com", "9876543217", "Flat 404, Pearl Apartments",
							base.plusSeconds(25200)),
					pending("Vikram Joshi", "vikram.j@gmail.com", "9876543218", "Block C, Metro Enclave",
							base.plusSeconds(28800)),
					pending("Riya Chatterjee", "riya.c@gmail.com", "9876543219", "Flat 56, Lotus Residency",
							base.plusSeconds(32400)),

					rejected("Fake User One", "fake1@gmail.com", "1111111111", "Incomplete address proof",
							base.plusSeconds(36000)),
					rejected("Spam Applicant", "spam@gmail.com", "2222222222", "Spam or bot submission",
							base.plusSeconds(39600)),
					rejected("Duplicate User", "dup@gmail.com", "3333333333", "Duplicate request detected",
							base.plusSeconds(43200)),
					rejected("Invalid Phone", "badphone@gmail.com", "12345", "Invalid phone number",
							base.plusSeconds(46800))));
		};
	}

	private ConsumerRequest pending(String name, String email, String phone, String address, Instant time) {
		return ConsumerRequest.builder().fullName(name).email(email).phone(phone).addressLine1(address)
				.city("Hyderabad").state("Telangana").postalCode("500081").status("PENDING").createdAt(time)
				.updatedAt(time).build();
	}

	private ConsumerRequest rejected(String name, String email, String phone, String reason, Instant time) {
		return ConsumerRequest.builder().fullName(name).email(email).phone(phone).addressLine1("Unknown Address")
				.city("Hyderabad").state("Telangana").postalCode("500081").status("REJECTED").rejectionReason(reason)
				.createdAt(time).updatedAt(time).build();
	}
}
