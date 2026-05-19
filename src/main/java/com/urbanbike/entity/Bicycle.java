package com.urbanbike.entity;

import com.urbanbike.enums.BikeStatus;
import com.urbanbike.enums.BikeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bicycle {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(unique = true, nullable = false)
	private String code;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BikeType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BikeStatus status;

}
