package com.utilitybilling.tariffservice.repository;

import com.utilitybilling.tariffservice.model.Tariff;
import com.utilitybilling.tariffservice.model.UtilityType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TariffRepository extends MongoRepository<Tariff, String> {

	Optional<Tariff> findByUtilityType(UtilityType utilityType);
}
