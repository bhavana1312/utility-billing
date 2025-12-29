package com.utilitybilling.meterservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "meter_readings")
public class MeterReading {

	@Id
	private String id;

	private String meterNumber;

	private double readingValue;

	private Instant readingDate = Instant.now();
}
