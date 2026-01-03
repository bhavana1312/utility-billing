package com.utilitybilling.billingservice.feign;

import lombok.Data;

@Data
public class MeterResponse{
    private String meterNumber;
    private String consumerId;
    private String utilityType;
    private String tariffPlan;
    private boolean active;
}
