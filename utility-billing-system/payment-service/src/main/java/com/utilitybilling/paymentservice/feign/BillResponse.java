package com.utilitybilling.paymentservice.feign;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import lombok.Data;

@Data
public class BillResponse {

	@Id
	private String id;
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

	private Instant dueDate;
	private Instant lastUpdatedAt;

	private BillStatus status;
	private Instant generatedAt = Instant.now();
}
