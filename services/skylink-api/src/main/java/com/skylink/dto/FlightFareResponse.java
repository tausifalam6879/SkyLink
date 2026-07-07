package com.skylink.dto;

import com.skylink.entity.FareClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightFareResponse {

    private Long id;

    private Long flightScheduleId;

    private String flightNumber;

    private String sourceIataCode;

    private String destinationIataCode;

    private FareClass fareClass;

    private BigDecimal baseFare;

    private Integer availableSeats;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}