package com.skylink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
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
public class PassengerRequest {

    @NotBlank(
            message = "Passenger first name is required."
    )
    private String firstName;

    @NotBlank(
            message = "Passenger last name is required."
    )
    private String lastName;

    @NotNull(
            message = "Passenger date of birth is required."
    )
    @Past(
            message = "Passenger date of birth must be in the past."
    )
    private LocalDate dateOfBirth;

    @NotBlank(
            message = "Passenger gender is required."
    )
    private String gender;

    private String passportNumber;

    @NotBlank(
            message = "Passenger nationality is required."
    )
    private String nationality;

    @NotBlank(
            message = "Seat number is required."
    )
    @Pattern(
            regexp = "^[0-9]{1,3}[A-Fa-f]$",
            message = "Invalid seat number."
    )
    private String seatNumber;
}