package com.urbanbike.service;

import java.math.BigDecimal;

public record RentalCostResult(
		int realHours,
		int delayHours,
		BigDecimal baseCost,
		BigDecimal penaltyAmount,
		BigDecimal totalCost) {
}
