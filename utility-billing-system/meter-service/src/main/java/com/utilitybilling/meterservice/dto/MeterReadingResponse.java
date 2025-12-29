package com.utilitybilling.meterservice.dto;

import lombok.Data;

@Data
public class MeterReadingResponse {
	private String meterNumber;
	private double previousReading;
	private double currentReading;
	private double unitsUsed;
}
