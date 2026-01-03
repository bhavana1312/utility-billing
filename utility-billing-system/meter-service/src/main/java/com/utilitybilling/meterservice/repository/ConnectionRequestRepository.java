package com.utilitybilling.meterservice.repository;

import com.utilitybilling.meterservice.model.ConnectionRequest;
import com.utilitybilling.meterservice.model.ConnectionStatus;
import com.utilitybilling.meterservice.model.UtilityType;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConnectionRequestRepository extends MongoRepository<ConnectionRequest, String> {

	List<ConnectionRequest> findByStatus(ConnectionStatus status);

	boolean existsByConsumerIdAndStatusInAndUtilityType(String consumerId, List<ConnectionStatus> of, UtilityType utilityType);
}
