package com.utilitybilling.paymentservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Document(collection = "payments")
public class Payment {

	@Id
	private String id;

	private String billId;
	private String consumerId;
	private String utilityType;

	private BigDecimal amount;

	private PaymentMode mode;
	private PaymentStatus status;

	private String otp;
	private Instant otpExpiresAt;

	private String processedBy;

	private Instant initiatedAt = Instant.now();
	private Instant completedAt;
}
