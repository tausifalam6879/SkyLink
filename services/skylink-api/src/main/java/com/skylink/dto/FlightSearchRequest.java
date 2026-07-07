package com.skylink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSearchRequest {

    @NotBlank(
            message = "Source IATA code is required."
    )
    private String sourceIataCode;

    @NotBlank(
            message = "Destination IATA code is required."
    )
    private String destinationIataCode;

    @NotNull(
            message = "Travel date is required."
    )
    private LocalDate travelDate;
}