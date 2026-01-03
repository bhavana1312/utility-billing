package com.utilitybilling.billingservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Data
@Document(collection = "bills")
public class Bill {

	@Id
	private String id;

	private String consumerId;
	private String meterNumber;
	private String utilityType;
	private String tariffPlan;

	private double previousReading;
	private double currentReading;
	private double unitsConsumed;

	private BigDecimal energyCharge;
	private BigDecimal fixedCharge;
	private BigDecimal taxAmount;
	private BigDecimal penaltyAmount;
	private BigDecimal totalAmount;

	private Date dueDate;
	private Instant lastUpdatedAt;

	private BillStatus status;
	private Instant generatedAt = Instant.now();
}
