package com.skylink.dto;

import com.skylink.entity.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSearchResponse {

    private Long flightScheduleId;

    private String flightNumber;

    private Long flightRouteId;

    private String sourceIataCode;

    private String sourceAirportName;

    private String sourceCity;

    private String sourceCountryCode;

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

    private LocalDateTime departureTime;

    private LocalDateTime arrivalTime;

    private FlightStatus status;

    private List<FlightFareResponse> fares;
}