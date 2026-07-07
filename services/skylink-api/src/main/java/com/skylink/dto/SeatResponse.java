package com.skylink.dto;

import com.skylink.entity.FareClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatResponse {

    private Long id;

    private String seatNumber;

    private FareClass fareClass;

    private Integer rowNumber;

    private String seatLetter;

    private boolean windowSeat;

    private boolean aisleSeat;

    private boolean extraLegroom;

    private boolean booked;

    private boolean available;
}