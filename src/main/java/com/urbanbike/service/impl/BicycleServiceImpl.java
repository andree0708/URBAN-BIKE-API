package com.urbanbike.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.urbanbike.dto.request.CreateBicycleRequest;
import com.urbanbike.dto.response.BicycleResponse;
import com.urbanbike.entity.Bicycle;
import com.urbanbike.enums.BikeStatus;
import com.urbanbike.enums.BikeType;
import com.urbanbike.exception.DuplicateBicycleCodeException;
import com.urbanbike.repository.BicycleRepository;
import com.urbanbike.service.BicycleService;

@Service
public class BicycleServiceImpl implements BicycleService {

	private final BicycleRepository bicycleRepository;

	public BicycleServiceImpl(BicycleRepository bicycleRepository) {
		this.bicycleRepository = bicycleRepository;
	}

	@Override
	public BicycleResponse createBicycle(CreateBicycleRequest request) {
		if (bicycleRepository.findByCode(request.getCode()).isPresent()) {
			throw new DuplicateBicycleCodeException(
					"Ya existe una bicicleta con el código " + request.getCode());
		}

		Bicycle bicycle = Bicycle.builder()
				.code(request.getCode())
				.type(request.getType())
				.status(BikeStatus.AVAILABLE)
				.build();

		Bicycle saved = bicycleRepository.save(bicycle);
		return toResponse(saved);
	}

	@Override
	public List<BicycleResponse> getAvailableBicycles(BikeType type) {
		List<Bicycle> bicycles = type == null
				? bicycleRepository.findByStatus(BikeStatus.AVAILABLE)
				: bicycleRepository.findByStatusAndType(BikeStatus.AVAILABLE, type);

		return bicycles.stream()
				.map(this::toResponse)
				.toList();
	}

	private BicycleResponse toResponse(Bicycle bicycle) {
		return new BicycleResponse(
				bicycle.getId(),
				bicycle.getCode(),
				bicycle.getType(),
				bicycle.getStatus());
	}

}
