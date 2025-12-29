package com.utilitybilling.billingservice.repository;

import com.utilitybilling.billingservice.model.Bill;
import com.utilitybilling.billingservice.model.BillStatus;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends MongoRepository<Bill, String> {

	Optional<Bill> findTopByMeterNumberOrderByGeneratedAtDesc(String meterNumber);

	List<Bill> findByStatusAndDueDateBefore(BillStatus status, Instant now);
}
