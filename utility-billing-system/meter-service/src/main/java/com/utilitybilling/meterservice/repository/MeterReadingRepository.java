package com.utilitybilling.meterservice.repository;

import com.utilitybilling.meterservice.model.MeterReading;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MeterReadingRepository extends MongoRepository<MeterReading,String>{

    Optional<MeterReading> findTopByMeterNumberOrderByReadingDateDesc(String meterNumber);
}
