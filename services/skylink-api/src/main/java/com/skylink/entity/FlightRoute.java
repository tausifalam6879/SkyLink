package com.skylink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "flight_routes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_route_airports",
                        columnNames = {
                                "source_airport_id",
                                "destination_airport_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_route_source",
                        columnList = "source_airport_id"
                ),
                @Index(
                        name = "idx_route_destination",
                        columnList = "destination_airport_id"
                ),
                @Index(
                        name = "idx_route_active",
                        columnList = "active"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "source_airport_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_route_source_airport"
            )
    )
    private Airport sourceAirport;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "destination_airport_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_route_destination_airport"
            )
    )
    private Airport destinationAirport;

    @Column(nullable = false)
    private Double distanceKm;

    private Integer estimatedDurationMinutes;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}