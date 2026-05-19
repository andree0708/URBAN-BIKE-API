package com.urbanbike.dto.request;

import com.urbanbike.enums.BikeType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBicycleRequest {

	@NotBlank
	private String code;

	@NotNull
	private BikeType type;

}
