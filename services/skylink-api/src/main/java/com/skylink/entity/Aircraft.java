package com.skylink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "aircraft",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_aircraft_registration_number",
                        columnNames = "registration_number"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "registration_number",
            nullable = false,
            unique = true,
            length = 50
    )
    private String registrationNumber;

    @Column(
            name = "manufacturer",
            nullable = false,
            length = 100
    )
    private String manufacturer;

    @Column(
            name = "model",
            nullable = false,
            length = 100
    )
    private String model;

    @Column(
            name = "total_seats",
            nullable = false
    )
    private Integer totalSeats;

    @Column(
            name = "economy_seats",
            nullable = false
    )
    private Integer economySeats;

    @Column(
            name = "business_seats",
            nullable = false
    )
    private Integer businessSeats;

    @Column(
            name = "first_class_seats",
            nullable = false
    )
    private Integer firstClassSeats;

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