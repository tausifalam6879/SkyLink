package com.skylink.repository;

import com.skylink.entity.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AircraftRepository
        extends JpaRepository<Aircraft, Long> {

    Optional<Aircraft> findByRegistrationNumberIgnoreCase(
            String registrationNumber
    );

    boolean existsByRegistrationNumberIgnoreCase(
            String registrationNumber
    );

    List<Aircraft> findByActiveTrue();

    List<Aircraft> findByManufacturerIgnoreCase(
            String manufacturer
    );

    List<Aircraft> findByModelIgnoreCase(
            String model
    );
}