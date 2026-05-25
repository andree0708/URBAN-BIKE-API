package com.urbanbike.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.urbanbike.dto.request.StartRentalRequest;
import com.urbanbike.dto.response.RentalResponse;
import com.urbanbike.entity.Bicycle;
import com.urbanbike.entity.Rental;
import com.urbanbike.enums.BikeStatus;
import com.urbanbike.enums.BikeType;
import com.urbanbike.exception.BikeNotAvailableException;
import com.urbanbike.exception.BikeNotFoundException;
import com.urbanbike.exception.RentalAlreadyFinishedException;
import com.urbanbike.exception.RentalNotFoundException;
import com.urbanbike.repository.BicycleRepository;
import com.urbanbike.repository.RentalRepository;
import com.urbanbike.service.impl.RentalServiceImpl;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {

	private void assertMoney(BigDecimal expected, BigDecimal actual) {
		assertEquals(0, expected.compareTo(actual));
	}

	@Mock
	private RentalRepository rentalRepository;

	@Mock
	private BicycleRepository bicycleRepository;

	@Mock
	private RentalCalculatorService rentalCalculatorService;

	@InjectMocks
	private RentalServiceImpl rentalService;

	@Test
	void startRental_success() {
		StartRentalRequest request = new StartRentalRequest("BIC-001", "Ana García", 2);
		Bicycle bicycle = Bicycle.builder()
				.id(1L)
				.code("BIC-001")
				.type(BikeType.URBAN)
				.status(BikeStatus.AVAILABLE)
				.build();

		when(bicycleRepository.findByCode("BIC-001")).thenReturn(Optional.of(bicycle));
		when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> {
			Rental rental = invocation.getArgument(0);
			rental.setId(10L);
			return rental;
		});

		RentalResponse response = rentalService.startRental(request);

		assertEquals("ACTIVE", response.getRentalStatus());
		assertEquals(BikeStatus.RENTED, bicycle.getStatus());
		verify(bicycleRepository).save(bicycle);
		verify(rentalRepository).save(any(Rental.class));
	}

	@Test
	void startRental_bikeNotFound() {
		StartRentalRequest request = new StartRentalRequest("BIC-999", "Ana García", 2);
		when(bicycleRepository.findByCode("BIC-999")).thenReturn(Optional.empty());

		assertThrows(BikeNotFoundException.class, () -> rentalService.startRental(request));
	}

	@Test
	void startRental_bikeNotAvailable() {
		StartRentalRequest request = new StartRentalRequest("BIC-001", "Ana García", 2);
		Bicycle bicycle = Bicycle.builder()
				.code("BIC-001")
				.type(BikeType.URBAN)
				.status(BikeStatus.RENTED)
				.build();
		when(bicycleRepository.findByCode("BIC-001")).thenReturn(Optional.of(bicycle));

		assertThrows(BikeNotAvailableException.class, () -> rentalService.startRental(request));
	}

	@Test
	void finishRental_success() {
		Bicycle bicycle = Bicycle.builder()
				.id(1L)
				.code("BIC-002")
				.type(BikeType.MOUNTAIN)
				.status(BikeStatus.RENTED)
				.build();
		Rental rental = Rental.builder()
				.id(5L)
				.bicycle(bicycle)
				.clientName("Carlos")
				.startTime(LocalDateTime.of(2024, 1, 1, 10, 0))
				.estimatedHours(2)
				.build();

		RentalCostResult costResult = new RentalCostResult(
				2, 0,
				BigDecimal.valueOf(10000),
				BigDecimal.ZERO,
				BigDecimal.valueOf(10000));

		when(rentalRepository.findById(5L)).thenReturn(Optional.of(rental));
		when(rentalCalculatorService.calculate(
				eq(BikeType.MOUNTAIN),
				eq(rental.getStartTime()),
				any(LocalDateTime.class),
				eq(2)))
				.thenReturn(costResult);
		when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

		RentalResponse response = rentalService.finishRental(5L);

		assertEquals("FINISHED", response.getRentalStatus());
		assertNotNull(rental.getEndTime());
		assertMoney(BigDecimal.valueOf(10000), rental.getBaseCost());
		assertMoney(BigDecimal.ZERO, rental.getPenaltyAmount());
		assertMoney(BigDecimal.valueOf(10000), rental.getTotalCost());
		assertEquals(false, rental.getPenaltyApplied());
		assertEquals(BikeStatus.AVAILABLE, bicycle.getStatus());
		verify(bicycleRepository).save(bicycle);
		verify(rentalRepository).save(rental);
	}

	@Test
	void finishRental_notFound() {
		when(rentalRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThrows(RentalNotFoundException.class, () -> rentalService.finishRental(99L));
	}

	@Test
	void finishRental_alreadyFinished() {
		Rental rental = Rental.builder()
				.id(5L)
				.endTime(LocalDateTime.now())
				.build();
		when(rentalRepository.findById(5L)).thenReturn(Optional.of(rental));

		assertThrows(RentalAlreadyFinishedException.class, () -> rentalService.finishRental(5L));
	}

}
