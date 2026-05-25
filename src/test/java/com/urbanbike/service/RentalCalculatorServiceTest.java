package com.urbanbike.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.urbanbike.enums.BikeType;

class RentalCalculatorServiceTest {

	private static final LocalDateTime START = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

	private RentalCalculatorService rentalCalculatorService;

	private void assertMoney(BigDecimal expected, BigDecimal actual) {
		assertEquals(0, expected.compareTo(actual));
	}

	@BeforeEach
	void setUp() {
		rentalCalculatorService = new RentalCalculatorService();
	}

	@Test
	void exactTwoHoursMountain() {
		LocalDateTime end = START.plusHours(2);
		int estimatedHours = 2;

		RentalCostResult result = rentalCalculatorService.calculate(
				BikeType.MOUNTAIN, START, end, estimatedHours);

		assertEquals(2, result.realHours());
		assertEquals(0, result.delayHours());
		assertMoney(BigDecimal.valueOf(10000), result.baseCost());
		assertMoney(BigDecimal.valueOf(0), result.penaltyAmount());
		assertMoney(BigDecimal.valueOf(10000), result.totalCost());
	}

	@Test
	void roundedHoursMountain() {
		LocalDateTime end = START.plusHours(1).plusMinutes(10);
		int estimatedHours = 2;

		RentalCostResult result = rentalCalculatorService.calculate(
				BikeType.MOUNTAIN, START, end, estimatedHours);

		assertEquals(2, result.realHours());
		assertEquals(0, result.delayHours());
		assertMoney(BigDecimal.valueOf(10000), result.baseCost());
		assertMoney(BigDecimal.valueOf(0), result.penaltyAmount());
		assertMoney(BigDecimal.valueOf(10000), result.totalCost());
	}

	@Test
	void penaltyCalculation() {
		LocalDateTime end = START.plusHours(3).plusMinutes(20);
		int estimatedHours = 2;

		RentalCostResult result = rentalCalculatorService.calculate(
				BikeType.MOUNTAIN, START, end, estimatedHours);

		assertEquals(4, result.realHours());
		assertEquals(2, result.delayHours());
		assertMoney(BigDecimal.valueOf(20000), result.baseCost());
		assertMoney(BigDecimal.valueOf(5000), result.penaltyAmount());
		assertMoney(BigDecimal.valueOf(25000), result.totalCost());
	}

	@Test
	void electricExactReturn() {
		LocalDateTime end = START.plusHours(2);
		int estimatedHours = 2;

		RentalCostResult result = rentalCalculatorService.calculate(
				BikeType.ELECTRIC, START, end, estimatedHours);

		assertEquals(2, result.realHours());
		assertEquals(0, result.delayHours());
		assertMoney(BigDecimal.valueOf(15000), result.baseCost());
		assertMoney(BigDecimal.valueOf(0), result.penaltyAmount());
		assertMoney(BigDecimal.valueOf(15000), result.totalCost());
	}

	@Test
	void minimumOneHourPenalty() {
		LocalDateTime end = START.plusHours(2).plusMinutes(10);
		int estimatedHours = 2;

		RentalCostResult result = rentalCalculatorService.calculate(
				BikeType.MOUNTAIN, START, end, estimatedHours);

		assertEquals(1, result.delayHours());
		assertMoney(BigDecimal.valueOf(2500), result.penaltyAmount());
	}

	@Test
	void urbanWithDelay() {
		LocalDateTime end = START.plusHours(1).plusMinutes(10);
		int estimatedHours = 1;

		RentalCostResult result = rentalCalculatorService.calculate(
				BikeType.URBAN, START, end, estimatedHours);

		assertEquals(2, result.realHours());
		assertEquals(1, result.delayHours());
		assertMoney(BigDecimal.valueOf(7000), result.baseCost());
		assertMoney(BigDecimal.valueOf(1750), result.penaltyAmount());
		assertMoney(BigDecimal.valueOf(8750), result.totalCost());
	}

	@Test
	void lessThanOneMinute() {
		LocalDateTime end = START.plusSeconds(30);
		int estimatedHours = 1;

		RentalCostResult result = rentalCalculatorService.calculate(
				BikeType.URBAN, START, end, estimatedHours);

		assertEquals(1, result.realHours());
		assertEquals(0, result.delayHours());
		assertMoney(BigDecimal.valueOf(3500), result.baseCost());
		assertMoney(BigDecimal.valueOf(0), result.penaltyAmount());
	}

}
