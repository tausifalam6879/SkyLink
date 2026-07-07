package com.skylink.repository;

import com.skylink.entity.FareClass;
import com.skylink.entity.FlightFare;
import com.skylink.entity.FlightSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightFareRepository
        extends JpaRepository<FlightFare, Long> {

    Optional<FlightFare>
    findByFlightScheduleAndFareClass(
            FlightSchedule flightSchedule,
            FareClass fareClass
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ff
            FROM FlightFare ff
            WHERE ff.flightSchedule.id = :flightScheduleId
            AND ff.fareClass = :fareClass
            """)
    Optional<FlightFare>
    findByFlightScheduleIdAndFareClassForUpdate(
            @Param("flightScheduleId")
            Long flightScheduleId,

            @Param("fareClass")
            FareClass fareClass
    );

    boolean existsByFlightScheduleAndFareClass(
            FlightSchedule flightSchedule,
            FareClass fareClass
    );

    List<FlightFare>
    findByFlightScheduleAndActiveTrue(
            FlightSchedule flightSchedule
    );

    List<FlightFare>
    findByFlightScheduleIdAndActiveTrue(
            Long flightScheduleId
    );

    List<FlightFare>
    findByActiveTrue();
}