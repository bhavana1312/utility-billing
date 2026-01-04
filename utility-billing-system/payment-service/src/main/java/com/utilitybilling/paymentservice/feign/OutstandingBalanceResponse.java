package com.utilitybilling.paymentservice.feign;

import lombok.Data;

@Data
public class OutstandingBalanceResponse {
	private String consumerId;
	private double outstandingAmount;
}
