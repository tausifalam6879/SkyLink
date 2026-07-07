package com.skylink.repository;

import com.skylink.entity.Booking;
import com.skylink.entity.BookingStatus;
import com.skylink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReferenceIgnoreCase(
            String bookingReference
    );

    boolean existsByBookingReferenceIgnoreCase(
            String bookingReference
    );

    List<Booking> findByUserOrderByCreatedAtDesc(
            User user
    );

    List<Booking> findByUserAndActiveTrueOrderByCreatedAtDesc(
            User user
    );

    List<Booking> findByStatus(
            BookingStatus status
    );

    List<Booking> findByActiveTrue();
}