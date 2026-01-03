package com.utilitybilling.billingservice.repository;

import com.utilitybilling.billingservice.model.Bill;
import com.utilitybilling.billingservice.model.BillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends MongoRepository<Bill, String> {

    Optional<Bill> findTopByMeterNumberOrderByGeneratedAtDesc(String meterNumber);

    List<Bill> findByStatusAndDueDateBefore(BillStatus status, Instant now);

    Page<Bill> findByConsumerIdOrderByGeneratedAtDesc(String consumerId, Pageable pageable);

    Optional<Bill> findByIdAndConsumerId(String id, String consumerId);

    Page<Bill> findByStatus(BillStatus status, Pageable pageable);

    Page<Bill> findAll(Pageable pageable);

    List<Bill> findByConsumerIdAndStatusIn(String consumerId, List<BillStatus> statuses);
}
