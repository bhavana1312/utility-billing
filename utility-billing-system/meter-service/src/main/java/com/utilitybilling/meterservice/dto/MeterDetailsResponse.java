package com.utilitybilling.meterservice.dto;

import com.utilitybilling.meterservice.model.UtilityType;
import lombok.Data;

@Data
public class MeterDetailsResponse{
    private String meterNumber;
    private String email;
    private String consumerId;
    private UtilityType utilityType;
    private boolean active;
    private double lastReading;
}
