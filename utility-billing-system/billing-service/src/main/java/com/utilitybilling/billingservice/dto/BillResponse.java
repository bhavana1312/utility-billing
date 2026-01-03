package com.utilitybilling.billingservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Data
public class BillResponse {

	private String billId;
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
	private String status;
	private Instant generatedAt;
}

