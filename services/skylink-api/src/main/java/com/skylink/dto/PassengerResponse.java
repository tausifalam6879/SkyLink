package com.skylink.dto;

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
public class PassengerResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private String gender;

    private String passportNumber;

    private String nationality;

    private String seatNumber;

    private boolean active;
}