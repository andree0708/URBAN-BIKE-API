package com.urbanbike.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import com.urbanbike.entity.Bicycle;
import com.urbanbike.enums.BikeStatus;
import com.urbanbike.enums.BikeType;
import com.urbanbike.repository.BicycleRepository;

@Configuration
public class DataInitializer implements CommandLineRunner {

	private final BicycleRepository bicycleRepository;

	public DataInitializer(BicycleRepository bicycleRepository) {
		this.bicycleRepository = bicycleRepository;
	}

	@Override
	public void run(String... args) {
		if (bicycleRepository.count() == 0) {
			bicycleRepository.saveAll(List.of(
					Bicycle.builder().code("BIC-001").type(BikeType.URBAN).status(BikeStatus.AVAILABLE).build(),
					Bicycle.builder().code("BIC-002").type(BikeType.MOUNTAIN).status(BikeStatus.AVAILABLE).build(),
					Bicycle.builder().code("BIC-003").type(BikeType.ELECTRIC).status(BikeStatus.AVAILABLE).build(),
					Bicycle.builder().code("BIC-004").type(BikeType.MOUNTAIN).status(BikeStatus.UNDER_MAINTENANCE)
							.build(),
					Bicycle.builder().code("BIC-005").type(BikeType.URBAN).status(BikeStatus.AVAILABLE).build()));
		}
	}

}
