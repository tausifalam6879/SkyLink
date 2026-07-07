package com.skylink.repository;

import com.skylink.entity.Airport;
import com.skylink.entity.FlightRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRouteRepository
        extends JpaRepository<FlightRoute, Long> {

    Optional<FlightRoute>
    findBySourceAirportAndDestinationAirport(
            Airport sourceAirport,
            Airport destinationAirport
    );

    boolean existsBySourceAirportAndDestinationAirport(
            Airport sourceAirport,
            Airport destinationAirport
    );

    Optional<FlightRoute>
    findBySourceAirportIataCodeIgnoreCaseAndDestinationAirportIataCodeIgnoreCase(
            String sourceIataCode,
            String destinationIataCode
    );

    List<FlightRoute>
    findBySourceAirportAndActiveTrue(
            Airport sourceAirport
    );

    List<FlightRoute>
    findByDestinationAirportAndActiveTrue(
            Airport destinationAirport
    );

    List<FlightRoute>
    findByActiveTrue();
}