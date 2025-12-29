package com.utilitybilling.meterservice.repository;

import com.utilitybilling.meterservice.model.Meter;
import com.utilitybilling.meterservice.model.UtilityType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MeterRepository extends MongoRepository<Meter, String> {

	Optional<Meter> findByConsumerIdAndUtilityTypeAndActiveTrue(String consumerId, UtilityType utilityType);

	List<Meter> findByConsumerId(String consumerId);
}
