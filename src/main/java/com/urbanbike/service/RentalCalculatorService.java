package com.urbanbike.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.urbanbike.enums.BikeType;

@Component
public class RentalCalculatorService {

	public RentalCostResult calculate(BikeType type, LocalDateTime startTime,
			LocalDateTime endTime, int estimatedHours) {
		long realMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
		int realHours = (int) Math.ceil(realMinutes / 60.0);
		if (realHours == 0) {
			realHours = 1;
		}

		long delayMinutes = realMinutes - (estimatedHours * 60L);
		int delayHours;
		BigDecimal penaltyAmount;

		if (delayMinutes > 0) {
			delayHours = (int) Math.ceil(delayMinutes / 60.0);
			if (delayHours == 0) {
				delayHours = 1;
			}
			penaltyAmount = type.getPenaltyRate().multiply(BigDecimal.valueOf(delayHours));
		} else {
			delayHours = 0;
			penaltyAmount = BigDecimal.ZERO;
		}

		BigDecimal baseCost = type.getHourlyRate().multiply(BigDecimal.valueOf(realHours));
		BigDecimal totalCost = baseCost.add(penaltyAmount);

		return new RentalCostResult(realHours, delayHours, baseCost, penaltyAmount, totalCost);
	}

}
