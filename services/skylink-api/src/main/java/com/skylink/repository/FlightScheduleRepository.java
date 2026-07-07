package com.skylink.repository;

import com.skylink.entity.Aircraft;
import com.skylink.entity.FlightRoute;
import com.skylink.entity.FlightSchedule;
import com.skylink.entity.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightScheduleRepository
        extends JpaRepository<FlightSchedule, Long> {

    Optional<FlightSchedule>
    findByFlightNumberIgnoreCase(
            String flightNumber
    );

    boolean existsByFlightNumberIgnoreCase(
            String flightNumber
    );

    boolean existsByFlightNumberIgnoreCaseAndDepartureTime(
            String flightNumber,
            LocalDateTime departureTime
    );

    boolean existsByFlightRouteAndDepartureTime(
            FlightRoute flightRoute,
            LocalDateTime departureTime
    );

    boolean
    existsByFlightRouteAndDepartureTimeBetween(
            FlightRoute flightRoute,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    boolean
    existsByAircraftAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
            Aircraft aircraft,
            LocalDateTime arrivalTime,
            LocalDateTime departureTime
    );

    List<FlightSchedule>
    findByFlightRouteSourceAirportIataCodeIgnoreCaseAndFlightRouteDestinationAirportIataCodeIgnoreCaseAndActiveTrueOrderByDepartureTimeAsc(
            String sourceIataCode,
            String destinationIataCode
    );

    List<FlightSchedule>
    findByFlightRouteSourceAirportIataCodeIgnoreCaseAndFlightRouteDestinationAirportIataCodeIgnoreCaseAndDepartureTimeBetweenAndActiveTrueOrderByDepartureTimeAsc(
            String sourceIataCode,
            String destinationIataCode,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    List<FlightSchedule>
    findByFlightRouteSourceAirportIataCodeIgnoreCaseAndFlightRouteDestinationAirportIataCodeIgnoreCaseAndDepartureTimeBetweenAndStatusAndActiveTrueOrderByDepartureTimeAsc(
            String sourceIataCode,
            String destinationIataCode,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            FlightStatus status
    );

    List<FlightSchedule>
    findByFlightRouteAndActiveTrue(
            FlightRoute flightRoute
    );

    List<FlightSchedule>
    findByAircraftAndActiveTrue(
            Aircraft aircraft
    );

    List<FlightSchedule>
    findByStatusAndActiveTrue(
            FlightStatus status
    );

    List<FlightSchedule>
    findByActiveTrue();

    long countByDepartureTimeBetween(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    long
    countByFlightRouteAndDepartureTimeBetween(
            FlightRoute flightRoute,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FlightSchedule>
    findWithLockById(
            Long id
    );
}