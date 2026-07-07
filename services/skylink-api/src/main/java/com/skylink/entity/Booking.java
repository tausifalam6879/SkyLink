package com.skylink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "bookings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_booking_reference",
                        columnNames = "booking_reference"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "booking_reference",
            nullable = false,
            unique = true,
            length = 20
    )
    private String bookingReference;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "flight_schedule_id",
            nullable = false
    )
    private FlightSchedule flightSchedule;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "flight_fare_id",
            nullable = false
    )
    private FlightFare flightFare;

    @Column(
            name = "passenger_count",
            nullable = false
    )
    private Integer passengerCount;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private BookingStatus status;

    @Column(
            nullable = false
    )
    private boolean active;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime currentTime =
                LocalDateTime.now();

        createdAt = currentTime;
        updatedAt = currentTime;

        if (status == null) {
            status = BookingStatus.PENDING;
        }

        if (!active) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}