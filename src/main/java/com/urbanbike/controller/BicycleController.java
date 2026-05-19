package com.urbanbike.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.urbanbike.dto.request.CreateBicycleRequest;
import com.urbanbike.dto.response.BicycleResponse;
import com.urbanbike.enums.BikeType;
import com.urbanbike.service.BicycleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/bicycles")
public class BicycleController {

	private final BicycleService bicycleService;

	public BicycleController(BicycleService bicycleService) {
		this.bicycleService = bicycleService;
	}

	@PostMapping
	public ResponseEntity<BicycleResponse> createBicycle(
			@RequestBody @Valid CreateBicycleRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(bicycleService.createBicycle(request));
	}

	@GetMapping("/available")
	public ResponseEntity<List<BicycleResponse>> getAvailableBicycles(
			@RequestParam(required = false) BikeType type) {
		return ResponseEntity.ok(bicycleService.getAvailableBicycles(type));
	}

}
