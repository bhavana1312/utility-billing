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
	private String meterNumber;
	private String utilityType;

	private double previousReading;
	private double currentReading;
	private double unitsConsumed;

	private BigDecimal energyCharge;
	private BigDecimal fixedCharge;
	private BigDecimal taxAmount;
	private BigDecimal penaltyAmount;
	private BigDecimal totalAmount;

	private PaymentMode paymentMode;
	private Instant paymentDate;

	private Instant billDueDate;
	private Instant billGeneratedAt;

	private Instant invoiceGeneratedAt = Instant.now();
}
