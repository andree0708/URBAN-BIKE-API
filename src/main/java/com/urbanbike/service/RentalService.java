package com.urbanbike.service;

import java.util.List;

import com.urbanbike.dto.request.StartRentalRequest;
import com.urbanbike.dto.response.RentalResponse;

public interface RentalService {

	RentalResponse startRental(StartRentalRequest request);

	RentalResponse finishRental(Long rentalId);

	List<RentalResponse> getRentalHistory(String bikeCode);

}
