package com.skylink.dto;

import com.skylink.entity.BookingStatus;
import com.skylink.entity.FareClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;

    private String bookingReference;

    private Long userId;

    private String userEmail;

    private Long flightScheduleId;

    private String flightNumber;

    private String sourceIataCode;

    private String sourceAirportName;

    private String destinationIataCode;

    private String destinationAirportName;

    private LocalDateTime departureTime;

    private LocalDateTime arrivalTime;

    private FareClass fareClass;

    private BigDecimal baseFare;

    private Integer passengerCount;

    private BigDecimal totalAmount;

    private BookingStatus status;

    private boolean active;

    private List<PassengerResponse> passengers;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}