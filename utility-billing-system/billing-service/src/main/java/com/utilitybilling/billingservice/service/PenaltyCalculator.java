package com.utilitybilling.billingservice.service;

import com.utilitybilling.billingservice.feign.OverduePenaltySlab;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class PenaltyCalculator {

	private PenaltyCalculator() {
	}

	public static BigDecimal calculatePenalty(BigDecimal baseAmount, int overdueDays, List<OverduePenaltySlab> slabs) {

		for (OverduePenaltySlab slab : slabs) {
			if (overdueDays >= slab.getFromDay() && overdueDays <= slab.getToDay()) {

				return baseAmount.multiply(BigDecimal.valueOf(slab.getPenaltyPercentage()))
						.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
			}
		}
		return BigDecimal.ZERO;
	}
}
