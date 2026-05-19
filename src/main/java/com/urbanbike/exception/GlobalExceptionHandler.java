package com.urbanbike.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.urbanbike.dto.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(DuplicateBicycleCodeException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateBicycleCode(DuplicateBicycleCodeException ex) {
		return buildResponse(ex, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(BikeNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleBikeNotFound(BikeNotFoundException ex) {
		return buildResponse(ex, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(BikeNotAvailableException.class)
	public ResponseEntity<ErrorResponse> handleBikeNotAvailable(BikeNotAvailableException ex) {
		return buildResponse(ex, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(RentalNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleRentalNotFound(RentalNotFoundException ex) {
		return buildResponse(ex, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(RentalAlreadyFinishedException.class)
	public ResponseEntity<ErrorResponse> handleRentalAlreadyFinished(RentalAlreadyFinishedException ex) {
		return buildResponse(ex, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));

		ErrorResponse body = new ErrorResponse(
				LocalDateTime.now(),
				HttpStatus.BAD_REQUEST.value(),
				ex.getClass().getSimpleName(),
				message);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	private ResponseEntity<ErrorResponse> buildResponse(RuntimeException ex, HttpStatus status) {
		ErrorResponse body = new ErrorResponse(
				LocalDateTime.now(),
				status.value(),
				ex.getClass().getSimpleName(),
				ex.getMessage());

		return ResponseEntity.status(status).body(body);
	}

}
