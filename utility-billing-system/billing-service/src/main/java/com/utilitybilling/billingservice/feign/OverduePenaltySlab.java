package com.utilitybilling.billingservice.feign;

import lombok.Data;

@Data
public class OverduePenaltySlab {
	private int fromDay;
	private int toDay;
	private double penaltyPercentage;
}
