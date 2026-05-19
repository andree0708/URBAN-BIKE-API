package com.urbanbike.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

	private LocalDateTime timestamp;
	private int httpStatus;
	private String error;
	private String message;

}
