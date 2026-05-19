package com.urbanbike.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.urbanbike.entity.Rental;

public interface RentalRepository extends JpaRepository<Rental, Long> {

	List<Rental> findByBicycleCode(String code);

	Optional<Rental> findByIdAndEndTimeIsNull(Long id);

}
