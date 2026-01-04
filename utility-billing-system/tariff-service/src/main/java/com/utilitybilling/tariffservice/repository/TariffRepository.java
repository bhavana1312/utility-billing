package com.utilitybilling.tariffservice.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.utilitybilling.tariffservice.model.Tariff;
import com.utilitybilling.tariffservice.model.UtilityType;

public interface TariffRepository extends MongoRepository<Tariff, String> {

	Optional<Tariff> findByUtilityType(UtilityType utilityType);
}
