package com.skylink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightRouteResponse {

    private Long id;

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

    private boolean active;
}