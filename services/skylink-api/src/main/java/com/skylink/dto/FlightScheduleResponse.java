package com.skylink.dto;

import com.skylink.entity.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightScheduleResponse {

    private Long id;

    private String flightNumber;

    private Long flightRouteId;

    private Long sourceAirportId;

    private String sourceIataCode;

    private String sourceAirportName;

    private String sourceCity;

    private String sourceCountryCode;

    private Long destinationAirportId;

    private String destinationIataCode;

    private String destinationAirportName;

    private String destinationCity;

    private String destinationCountryCode;

    private Double distanceKm;

    private Integer estimatedDurationMinutes;

    private Long aircraftId;

    private String aircraftRegistrationNumber;

    private String aircraftManufacturer;

    private String aircraftModel;

    private Integer totalSeats;

    private Integer economySeats;

    private Integer businessSeats;

    private Integer firstClassSeats;

    private LocalDateTime departureTime;

    private LocalDateTime arrivalTime;

    private FlightStatus status;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}