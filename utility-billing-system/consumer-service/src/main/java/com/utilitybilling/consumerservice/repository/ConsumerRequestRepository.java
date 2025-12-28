package com.utilitybilling.consumerservice.repository;

import com.utilitybilling.consumerservice.model.ConsumerRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConsumerRequestRepository extends MongoRepository<ConsumerRequest, String> {

	List<ConsumerRequest> findByStatus(String status);

	boolean existsByEmailAndStatusIn(String email, Iterable<String> statuses);
}
