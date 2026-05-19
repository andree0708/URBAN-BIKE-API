package com.urbanbike.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalResponse {

	private Long id;
	private String bikeCode;
	private String clientName;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private Integer estimatedHours;
	private Integer realHours;
	private BigDecimal baseCost;
	private BigDecimal penaltyAmount;
	private BigDecimal totalCost;
	private Boolean penaltyApplied;
	private String rentalStatus;

}
