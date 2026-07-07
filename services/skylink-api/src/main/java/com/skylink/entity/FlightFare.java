package com.skylink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "flight_fares",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_flight_fare_schedule_class",
                        columnNames = {
                                "flight_schedule_id",
                                "fare_class"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightFare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "flight_schedule_id",
            nullable = false
    )
    private FlightSchedule flightSchedule;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "fare_class",
            nullable = false,
            length = 30
    )
    private FareClass fareClass;

    @Column(
            name = "base_fare",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal baseFare;

    @Column(
            name = "available_seats",
            nullable = false
    )
    private Integer availableSeats;

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