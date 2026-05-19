package com.urbanbike.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urbanbike.dto.request.StartRentalRequest;
import com.urbanbike.dto.response.RentalResponse;
import com.urbanbike.service.RentalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/rentals")
public class RentalController {

	private final RentalService rentalService;

	public RentalController(RentalService rentalService) {
		this.rentalService = rentalService;
	}

	@PostMapping
	public ResponseEntity<RentalResponse> startRental(
			@RequestBody @Valid StartRentalRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(rentalService.startRental(request));
	}

	@PutMapping("/{id}/finish")
	public ResponseEntity<RentalResponse> finishRental(@PathVariable Long id) {
		return ResponseEntity.ok(rentalService.finishRental(id));
	}

	@GetMapping("/bicycle/{code}/history")
	public ResponseEntity<List<RentalResponse>> getRentalHistory(
			@PathVariable String code) {
		return ResponseEntity.ok(rentalService.getRentalHistory(code));
	}

}
