package com.urbanbike.enums;

import java.math.BigDecimal;

public enum BikeType {

	URBAN(new BigDecimal("3500")),
	MOUNTAIN(new BigDecimal("5000")),
	ELECTRIC(new BigDecimal("7500"));

	private final BigDecimal hourlyRate;

	BikeType(BigDecimal hourlyRate) {
		this.hourlyRate = hourlyRate;
	}

	public BigDecimal getHourlyRate() {
		return hourlyRate;
	}

	public BigDecimal getPenaltyRate() {
		return hourlyRate.multiply(new BigDecimal("0.5"));
	}

}
