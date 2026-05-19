package com.urbanbike.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rental {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	@JoinColumn(nullable = false)
	private Bicycle bicycle;

	@Column(nullable = false)
	private String clientName;

	@Column(nullable = false)
	private LocalDateTime startTime;

	@Column(nullable = false)
	private Integer estimatedHours;

	@Column(nullable = true)
	private LocalDateTime endTime;

	@Column(nullable = true)
	private BigDecimal baseCost;

	@Column(nullable = true)
	private BigDecimal penaltyAmount;

	@Column(nullable = true)
	private BigDecimal totalCost;

	@Column(nullable = false)
	@Builder.Default
	private Boolean penaltyApplied = false;

}
