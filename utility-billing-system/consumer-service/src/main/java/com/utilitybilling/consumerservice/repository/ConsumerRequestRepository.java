package com.utilitybilling.consumerservice.repository;

import com.utilitybilling.consumerservice.model.ConsumerRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConsumerRequestRepository extends MongoRepository<ConsumerRequest, String> {

	List<ConsumerRequest> findByStatus(String status);

	boolean existsByEmailAndStatusIn(String email, Iterable<String> statuses);

	Page<ConsumerRequest> findAll(Pageable pageable);

	Page<ConsumerRequest> findByStatus(String status, Pageable pageable);
}
