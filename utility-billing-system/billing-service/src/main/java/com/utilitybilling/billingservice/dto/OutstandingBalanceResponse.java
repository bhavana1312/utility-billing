package com.utilitybilling.billingservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OutstandingBalanceResponse {
	private String consumerId;
	private BigDecimal outstandingAmount;
}
