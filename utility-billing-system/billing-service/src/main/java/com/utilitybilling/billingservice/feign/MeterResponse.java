package com.utilitybilling.billingservice.feign;

import lombok.Data;

@Data
public class MeterResponse{
    private String meterNumber;
    private String email;
    private String consumerId;
    private String utilityType;
    private boolean active;
}
