package com.skylink.dto;

import com.skylink.entity.FareClass;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {

    @NotNull(
            message = "Flight schedule ID is required."
    )
    private Long flightScheduleId;

    @NotNull(
            message = "Fare class is required."
    )
    private FareClass fareClass;

    @NotEmpty(
            message = "At least one passenger is required."
    )
    @Valid
    private List<PassengerRequest> passengers;
}