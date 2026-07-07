package com.skylink.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFlightRouteRequest {

    @NotBlank(
            message = "Source IATA code is required."
    )
    private String sourceIataCode;

    @NotBlank(
            message = "Destination IATA code is required."
    )
    private String destinationIataCode;
}