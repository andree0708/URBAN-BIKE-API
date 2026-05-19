package com.urbanbike.service;

import java.util.List;

import com.urbanbike.dto.request.CreateBicycleRequest;
import com.urbanbike.dto.response.BicycleResponse;
import com.urbanbike.enums.BikeType;

public interface BicycleService {

	BicycleResponse createBicycle(CreateBicycleRequest request);

	List<BicycleResponse> getAvailableBicycles(BikeType type);

}
