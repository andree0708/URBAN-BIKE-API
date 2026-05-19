package com.urbanbike.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urbanbike.dto.request.StartRentalRequest;
import com.urbanbike.dto.response.RentalResponse;
import com.urbanbike.entity.Bicycle;
import com.urbanbike.entity.Rental;
import com.urbanbike.enums.BikeStatus;
import com.urbanbike.exception.BikeNotAvailableException;
import com.urbanbike.exception.BikeNotFoundException;
import com.urbanbike.exception.RentalAlreadyFinishedException;
import com.urbanbike.exception.RentalNotFoundException;
import com.urbanbike.repository.BicycleRepository;
import com.urbanbike.repository.RentalRepository;
import com.urbanbike.service.RentalCalculatorService;
import com.urbanbike.service.RentalCostResult;
import com.urbanbike.service.RentalService;

@Service
public class RentalServiceImpl implements RentalService {

	private final RentalRepository rentalRepository;
	private final BicycleRepository bicycleRepository;
	private final RentalCalculatorService rentalCalculatorService;

	public RentalServiceImpl(RentalRepository rentalRepository,
			BicycleRepository bicycleRepository,
			RentalCalculatorService rentalCalculatorService) {
		this.rentalRepository = rentalRepository;
		this.bicycleRepository = bicycleRepository;
		this.rentalCalculatorService = rentalCalculatorService;
	}

	@Override
	@Transactional
	public RentalResponse startRental(StartRentalRequest request) {
		String code = request.getBikeCode();

		Bicycle bicycle = bicycleRepository.findByCode(code)
				.orElseThrow(() -> new BikeNotFoundException(
						"La bicicleta con código " + code + " no fue encontrada"));

		if (bicycle.getStatus() != BikeStatus.AVAILABLE) {
			throw new BikeNotAvailableException(
					"La bicicleta " + code + " no está disponible para alquiler");
		}

		Rental rental = Rental.builder()
				.bicycle(bicycle)
				.clientName(request.getClientName())
				.startTime(LocalDateTime.now())
				.estimatedHours(request.getEstimatedHours())
				.penaltyApplied(false)
				.build();

		bicycle.setStatus(BikeStatus.RENTED);
		bicycleRepository.save(bicycle);

		Rental saved = rentalRepository.save(rental);
		return toRentalResponse(saved, "ACTIVE", null);
	}

	@Override
	@Transactional
	public RentalResponse finishRental(Long rentalId) {
		Rental rental = rentalRepository.findById(rentalId)
				.orElseThrow(() -> new RentalNotFoundException(
						"El alquiler con id " + rentalId + " no fue encontrado"));

		if (rental.getEndTime() != null) {
			throw new RentalAlreadyFinishedException(
					"El alquiler con id " + rentalId + " ya fue finalizado");
		}

		LocalDateTime endTime = LocalDateTime.now();
		rental.setEndTime(endTime);

		Bicycle bicycle = rental.getBicycle();
		RentalCostResult result = rentalCalculatorService.calculate(
				bicycle.getType(),
				rental.getStartTime(),
				endTime,
				rental.getEstimatedHours());

		rental.setBaseCost(result.baseCost());
		rental.setPenaltyAmount(result.penaltyAmount());
		rental.setTotalCost(result.totalCost());
		rental.setPenaltyApplied(result.delayHours() > 0);

		bicycle.setStatus(BikeStatus.AVAILABLE);
		bicycleRepository.save(bicycle);

		Rental saved = rentalRepository.save(rental);
		return toRentalResponse(saved, "FINISHED", result.realHours());
	}

	@Override
	public List<RentalResponse> getRentalHistory(String bikeCode) {
		bicycleRepository.findByCode(bikeCode)
				.orElseThrow(() -> new BikeNotFoundException(
						"La bicicleta con código " + bikeCode + " no fue encontrada"));

		return rentalRepository.findByBicycleCode(bikeCode).stream()
				.map(this::toHistoryResponse)
				.toList();
	}

	private RentalResponse toHistoryResponse(Rental rental) {
		if (rental.getEndTime() == null) {
			return toRentalResponse(rental, "ACTIVE", null);
		}

		RentalCostResult result = rentalCalculatorService.calculate(
				rental.getBicycle().getType(),
				rental.getStartTime(),
				rental.getEndTime(),
				rental.getEstimatedHours());

		return toRentalResponse(rental, "FINISHED", result.realHours());
	}

	private RentalResponse toRentalResponse(Rental rental, String rentalStatus, Integer realHours) {
		return new RentalResponse(
				rental.getId(),
				rental.getBicycle().getCode(),
				rental.getClientName(),
				rental.getStartTime(),
				rental.getEndTime(),
				rental.getEstimatedHours(),
				realHours,
				rental.getBaseCost(),
				rental.getPenaltyAmount(),
				rental.getTotalCost(),
				rental.getPenaltyApplied(),
				rentalStatus);
	}

}
