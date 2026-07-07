package com.skylink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "flight_schedules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_flight_schedule_flight_number_departure",
                        columnNames = {
                                "flight_number",
                                "departure_time"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "flight_number",
            nullable = false,
            length = 20
    )
    private String flightNumber;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "flight_route_id",
            nullable = false
    )
    private FlightRoute flightRoute;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "aircraft_id",
            nullable = false
    )
    private Aircraft aircraft;

    @Column(
            name = "departure_time",
            nullable = false
    )
    private LocalDateTime departureTime;

    @Column(
            name = "arrival_time",
            nullable = false
    )
    private LocalDateTime arrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private FlightStatus status;

    @Column(
            name = "active",
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
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}