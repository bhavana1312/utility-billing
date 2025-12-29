package com.utilitybilling.paymentservice.repository;

import com.utilitybilling.paymentservice.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InvoiceRepository extends MongoRepository<Invoice,String>{
    List<Invoice> findByConsumerId(String consumerId);
}
