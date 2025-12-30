package com.utilitybilling.paymentservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Document(collection = "invoices")
public class Invoice {

	@Id
	private String id;

	private String paymentId;
	private String billId;
	private String consumerId;

	private BigDecimal amount;
	private PaymentMode mode;

	private Instant generatedAt = Instant.now();
}
