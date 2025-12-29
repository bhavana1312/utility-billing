package com.utilitybilling.billingservice.repository;

import com.utilitybilling.billingservice.model.Bill;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BillRepository extends MongoRepository<Bill,String>{

    Optional<Bill> findTopByMeterNumberOrderByGeneratedAtDesc(String meterNumber);
}
