package com.utilitybilling.consumerservice.repository;

import com.utilitybilling.consumerservice.model.Consumer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConsumerRepository extends MongoRepository<Consumer,String>{
}
