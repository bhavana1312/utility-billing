package com.utilitybilling.paymentservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class InvoicePdfData {

	private String invoiceId;
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

	private Instant billGeneratedAt;
	private Instant billDueDate;
	private Instant paymentDate;
}
