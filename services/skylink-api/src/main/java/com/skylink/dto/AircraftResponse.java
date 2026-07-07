package com.skylink.dto;

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
public class AircraftResponse {

    private Long id;

    private String registrationNumber;

    private String manufacturer;

    private String model;

    private Integer totalSeats;

    private Integer economySeats;

    private Integer businessSeats;

    private Integer firstClassSeats;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}