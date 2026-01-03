package com.utilitybilling.meterservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "meters")
public class Meter {

	@Id
	private String meterNumber;

	private String consumerId;

	private UtilityType utilityType;
	
	private TariffPlan tariffPlan;

	private double lastReading;

	private Instant installationDate;

	private boolean active;
}
