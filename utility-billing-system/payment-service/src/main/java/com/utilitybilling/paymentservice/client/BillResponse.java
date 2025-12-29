package com.utilitybilling.paymentservice.client;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import lombok.Data;

@Data
public class BillResponse {

	@Id
	private String billId;

	private String consumerId;
	private String meterNumber;
	private String utilityType;

	private double previousReading;
	private double currentReading;
	private double unitsConsumed;

	private double energyCharge;
	private double fixedCharge;
	private double taxAmount;

	private double penaltyAmount;
	private double totalAmount;

	private Instant dueDate;
	private Instant lastUpdatedAt;

	private String status;
	private Instant generatedAt = Instant.now();
}
