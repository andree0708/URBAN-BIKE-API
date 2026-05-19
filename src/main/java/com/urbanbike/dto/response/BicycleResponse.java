package com.urbanbike.dto.response;

import com.urbanbike.enums.BikeStatus;
import com.urbanbike.enums.BikeType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BicycleResponse {

	private Long id;
	private String code;
	private BikeType type;
	private BikeStatus status;

}
