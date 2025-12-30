package com.utilitybilling.billingservice.dto;

import lombok.Data;
import java.util.Date;

import java.time.Instant;

@Data
public class BillResponse {

	private String billId;
	private String meterNumber;
	private String utilityType;

	private double previousReading;
	private double currentReading;
	private double unitsConsumed;

	private double energyCharge;
	private double fixedCharge;
	private double taxAmount;
	private double totalAmount;
	private double penaltyAmount;

	private Date dueDate;

	private String status;
	private Instant generatedAt;
}
