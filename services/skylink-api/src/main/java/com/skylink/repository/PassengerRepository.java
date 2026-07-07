package com.skylink.repository;

import com.skylink.entity.Booking;
import com.skylink.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerRepository
        extends JpaRepository<Passenger, Long> {

    List<Passenger> findByBooking(
            Booking booking
    );

    List<Passenger> findByBookingOrderByIdAsc(
            Booking booking
    );

    List<Passenger>
    findByBookingAndActiveTrueOrderByIdAsc(
            Booking booking
    );

    List<Passenger>
    findByBookingIdAndActiveTrueOrderByIdAsc(
            Long bookingId
    );

    long countByBookingAndActiveTrue(
            Booking booking
    );

    boolean
    existsByBookingFlightScheduleIdAndSeatNumberIgnoreCase(
            Long flightScheduleId,
            String seatNumber
    );
}