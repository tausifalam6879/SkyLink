package com.skylink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "airports",
        indexes = {
                @Index(
                        name = "idx_airport_external_id",
                        columnList = "external_airport_id"
                ),
                @Index(
                        name = "idx_airport_city",
                        columnList = "city"
                ),
                @Index(
                        name = "idx_airport_country_code",
                        columnList = "country_code"
                ),
                @Index(
                        name = "idx_airport_type",
                        columnList = "airport_type"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airport {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            name = "external_airport_id",
            unique = true,
            nullable = false
    )
    private Long externalAirportId;

    @Column(
            name = "iata_code",
            unique = true,
            length = 3
    )
    private String iataCode;

    @Column(
            name = "icao_code",
            unique = true,
            length = 4
    )
    private String icaoCode;

    @Column(
            name = "airport_name",
            nullable = false
    )
    private String airportName;

    @Column(
            name = "city"
    )
    private String city;

    @Column(
            name = "country"
    )
    private String country;

    @Column(
            name = "country_code",
            length = 2
    )
    private String countryCode;

    @Column(
            name = "airport_type"
    )
    private String airportType;

    private Double latitude;

    private Double longitude;

    private Double elevation;

    private String timezone;

    @Builder.Default
    private boolean active = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}