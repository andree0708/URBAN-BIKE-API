package com.urbanbike.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.urbanbike.entity.Bicycle;
import com.urbanbike.enums.BikeStatus;
import com.urbanbike.enums.BikeType;

public interface BicycleRepository extends JpaRepository<Bicycle, Long> {

	Optional<Bicycle> findByCode(String code);

	List<Bicycle> findByStatus(BikeStatus status);

	List<Bicycle> findByStatusAndType(BikeStatus status, BikeType type);

}
