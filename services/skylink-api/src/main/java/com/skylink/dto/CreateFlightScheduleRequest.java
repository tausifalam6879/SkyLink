package com.skylink.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateFlightScheduleRequest {

    @NotBlank(message = "Flight number is required.")
    private String flightNumber;

    @NotBlank(message = "Source IATA code is required.")
    private String sourceIataCode;

    @NotBlank(message = "Destination IATA code is required.")
    private String destinationIataCode;

    @NotNull(message = "Aircraft ID is required.")
    private Long aircraftId;

    @NotNull(message = "Departure time is required.")
    @Future(message = "Departure time must be in the future.")
    private LocalDateTime departureTime;
}