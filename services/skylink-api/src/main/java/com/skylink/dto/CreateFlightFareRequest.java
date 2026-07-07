package com.skylink.dto;

import com.skylink.entity.FareClass;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFlightFareRequest {

    @NotNull(
            message = "Flight schedule ID is required."
    )
    private Long flightScheduleId;

    @NotNull(
            message = "Fare class is required."
    )
    private FareClass fareClass;

    @NotNull(
            message = "Base fare is required."
    )
    @DecimalMin(
            value = "0.01",
            message = "Base fare must be greater than 0."
    )
    private BigDecimal baseFare;

    @NotNull(
            message = "Available seats are required."
    )
    @Min(
            value = 0,
            message = "Available seats cannot be negative."
    )
    private Integer availableSeats;
}