package com.skylink.controller;

import com.skylink.dto.SeatResponse;
import com.skylink.entity.FareClass;
import com.skylink.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping(
            "/flight/{flightScheduleId}"
    )
    public ResponseEntity<List<SeatResponse>>
    getAllSeats(
            @PathVariable
            Long flightScheduleId
    ) {

        List<SeatResponse> seats =
                seatService.getAllSeats(
                        flightScheduleId
                );

        return ResponseEntity.ok(
                seats
        );
    }

    @GetMapping(
            "/flight/{flightScheduleId}/class/{fareClass}"
    )
    public ResponseEntity<List<SeatResponse>>
    getSeatsByFareClass(
            @PathVariable
            Long flightScheduleId,

            @PathVariable
            FareClass fareClass
    ) {

        List<SeatResponse> seats =
                seatService.getSeats(
                        flightScheduleId,
                        fareClass
                );

        return ResponseEntity.ok(
                seats
        );
    }

    @GetMapping(
            "/flight/{flightScheduleId}/class/{fareClass}/available-count"
    )
    public ResponseEntity<Long>
    getAvailableSeatCount(
            @PathVariable
            Long flightScheduleId,

            @PathVariable
            FareClass fareClass
    ) {

        long availableSeatCount =
                seatService
                        .getAvailableSeatCount(
                                flightScheduleId,
                                fareClass
                        );

        return ResponseEntity.ok(
                availableSeatCount
        );
    }
}