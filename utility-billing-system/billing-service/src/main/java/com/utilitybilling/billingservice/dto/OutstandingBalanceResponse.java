package com.utilitybilling.billingservice.dto;

import lombok.Data;

@Data
public class OutstandingBalanceResponse {
    private String consumerId;
    private double outstandingAmount;
}
