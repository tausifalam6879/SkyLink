package com.skylink.repository;

import com.skylink.entity.FareClass;
import com.skylink.entity.FlightSchedule;
import com.skylink.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository
        extends JpaRepository<Seat, Long> {

    List<Seat> findByFlightScheduleAndFareClassAndActiveTrueOrderByRowNumberAscSeatLetterAsc(
            FlightSchedule flightSchedule,
            FareClass fareClass
    );

    List<Seat> findByFlightScheduleAndActiveTrueOrderByRowNumberAscSeatLetterAsc(
            FlightSchedule flightSchedule
    );

    Optional<Seat> findByFlightScheduleAndSeatNumberIgnoreCase(
            FlightSchedule flightSchedule,
            String seatNumber
    );

    boolean existsByFlightScheduleAndSeatNumberIgnoreCase(
            FlightSchedule flightSchedule,
            String seatNumber
    );

    long countByFlightScheduleAndFareClassAndBookedFalseAndActiveTrue(
            FlightSchedule flightSchedule,
            FareClass fareClass
    );

    List<Seat> findByFlightScheduleAndFareClassAndBookedFalseAndActiveTrueOrderByRowNumberAscSeatLetterAsc(
            FlightSchedule flightSchedule,
            FareClass fareClass
    );
}