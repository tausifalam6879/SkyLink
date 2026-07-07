package com.skylink.controller;

import com.skylink.dto.CreateFlightScheduleRequest;
import com.skylink.dto.FlightScheduleResponse;
import com.skylink.service.FlightScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flight-schedules")
@CrossOrigin("*")
public class FlightScheduleController {

    private final FlightScheduleService flightScheduleService;

    public FlightScheduleController(
            FlightScheduleService flightScheduleService
    ) {
        this.flightScheduleService =
                flightScheduleService;
    }

    @PostMapping
    public ResponseEntity<FlightScheduleResponse> createFlightSchedule(
            @Valid
            @RequestBody CreateFlightScheduleRequest request
    ) {

        FlightScheduleResponse response =
                flightScheduleService.createFlightSchedule(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<FlightScheduleResponse>>
    getAllFlightSchedules() {

        List<FlightScheduleResponse> responses =
                flightScheduleService
                        .getAllFlightSchedules();

        return ResponseEntity.ok(
                responses
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<FlightScheduleResponse>>
    getActiveFlightSchedules() {

        List<FlightScheduleResponse> responses =
                flightScheduleService
                        .getActiveFlightSchedules();

        return ResponseEntity.ok(
                responses
        );
    }

    @GetMapping("/{flightScheduleId}")
    public ResponseEntity<FlightScheduleResponse>
    getFlightScheduleById(
            @PathVariable Long flightScheduleId
    ) {

        FlightScheduleResponse response =
                flightScheduleService
                        .getFlightScheduleById(
                                flightScheduleId
                        );

        return ResponseEntity.ok(
                response
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<FlightScheduleResponse>>
    searchFlights(
            @RequestParam String sourceIataCode,
            @RequestParam String destinationIataCode,
            @RequestParam LocalDate travelDate
    ) {

        List<FlightScheduleResponse> responses =
                flightScheduleService.searchFlights(
                        sourceIataCode,
                        destinationIataCode,
                        travelDate
                );

        return ResponseEntity.ok(
                responses
        );
    }
}