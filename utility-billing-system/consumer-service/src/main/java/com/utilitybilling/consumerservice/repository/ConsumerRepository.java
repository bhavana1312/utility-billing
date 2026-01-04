package com.utilitybilling.consumerservice.repository;

import com.utilitybilling.consumerservice.model.Consumer;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConsumerRepository extends MongoRepository<Consumer,String>{
	
	Optional<Consumer> findByFullName(String username);
	
	Page<Consumer> findAll(Pageable pageable);
}


