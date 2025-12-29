package com.utilitybilling.paymentservice.repository;

import com.google.common.base.Optional;
import com.utilitybilling.paymentservice.model.Payment;
import com.utilitybilling.paymentservice.model.PaymentStatus;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {
	List<Payment> findByConsumerId(String consumerId);

	Optional<Payment> findByBillIdAndStatus(String billId, PaymentStatus status);
}
