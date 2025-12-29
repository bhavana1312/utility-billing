package com.utilitybilling.paymentservice.repository;

import com.utilitybilling.paymentservice.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment,String>{
    List<Payment> findByConsumerId(String consumerId);
}
