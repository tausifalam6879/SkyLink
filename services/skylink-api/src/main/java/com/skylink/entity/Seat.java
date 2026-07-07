package com.skylink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seat_schedule_number",
                        columnNames = {
                                "flight_schedule_id",
                                "seat_number"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_seat_flight_schedule",
                        columnList = "flight_schedule_id"
                ),
                @Index(
                        name = "idx_seat_schedule_fare_class",
                        columnList =
                                "flight_schedule_id,fare_class"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
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

    @Column(
            name = "seat_number",
            nullable = false,
            length = 10
    )
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "fare_class",
            nullable = false,
            length = 30
    )
    private FareClass fareClass;

    @Column(
            name = "seat_row",
            nullable = false
    )
    private Integer rowNumber;

    @Column(
            name = "seat_letter",
            nullable = false,
            length = 5
    )
    private String seatLetter;

    @Column(
            name = "window_seat",
            nullable = false
    )
    private boolean windowSeat;

    @Column(
            name = "aisle_seat",
            nullable = false
    )
    private boolean aisleSeat;

    @Column(
            name = "extra_legroom",
            nullable = false
    )
    private boolean extraLegroom;

    @Column(
            name = "booked",
            nullable = false
    )
    private boolean booked;

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

        active = true;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}