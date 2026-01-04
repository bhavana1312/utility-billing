package com.utilitybilling.paymentservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.utilitybilling.paymentservice.model.Payment;

public interface PaymentRepository extends MongoRepository<Payment, String> {

	Page<Payment> findByConsumerId(String consumerId, Pageable pageable);

	Page<Payment> findByConsumerIdAndUtilityType(String consumerId, String utilityType, Pageable pageable);

	@Query("""
			{
			  $and: [
			    {
			      $or: [
			        { billId: { $regex: ?0, $options: 'i' } },
			        { email: { $regex: ?0, $options: 'i' } }
			      ]
			    },
			    { mode: { $regex: ?1, $options: 'i' } }
			  ]
			}
			""")
	Page<Payment> search(String search, String mode, Pageable pageable);
}
