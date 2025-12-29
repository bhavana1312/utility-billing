package com.utilitybilling.paymentservice.client;

import lombok.Data;

@Data
public class OutstandingBalanceResponse {
	private String consumerId;
	private double outstandingAmount;
}
