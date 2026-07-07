package com.skylink.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAircraftRequest {

    @NotBlank(message = "Registration number is required.")
    private String registrationNumber;

    @NotBlank(message = "Manufacturer is required.")
    private String manufacturer;

    @NotBlank(message = "Aircraft model is required.")
    private String model;

    @NotNull(message = "Economy seats are required.")
    @Min(
            value = 0,
            message = "Economy seats cannot be negative."
    )
    private Integer economySeats;

    @NotNull(message = "Business seats are required.")
    @Min(
            value = 0,
            message = "Business seats cannot be negative."
    )
    private Integer businessSeats;

    @NotNull(message = "First class seats are required.")
    @Min(
            value = 0,
            message = "First class seats cannot be negative."
    )
    private Integer firstClassSeats;
}