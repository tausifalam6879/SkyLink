package com.skylink.controller;

import com.skylink.dto.CreateFlightFareRequest;
import com.skylink.dto.FlightFareResponse;
import com.skylink.service.FlightFareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flight-fares")
@CrossOrigin("*")
public class FlightFareController {

    private final FlightFareService flightFareService;

    public FlightFareController(
            FlightFareService flightFareService
    ) {
        this.flightFareService =
                flightFareService;
    }

    @PostMapping
    public ResponseEntity<FlightFareResponse> createFlightFare(
            @Valid
            @RequestBody CreateFlightFareRequest request
    ) {

        FlightFareResponse response =
                flightFareService.createFlightFare(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<FlightFareResponse>>
    getAllFlightFares() {

        List<FlightFareResponse> responses =
                flightFareService
                        .getAllFlightFares();

        return ResponseEntity.ok(
                responses
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<FlightFareResponse>>
    getActiveFlightFares() {

        List<FlightFareResponse> responses =
                flightFareService
                        .getActiveFlightFares();

        return ResponseEntity.ok(
                responses
        );
    }

    @GetMapping("/{flightFareId}")
    public ResponseEntity<FlightFareResponse>
    getFlightFareById(
            @PathVariable Long flightFareId
    ) {

        FlightFareResponse response =
                flightFareService
                        .getFlightFareById(
                                flightFareId
                        );

        return ResponseEntity.ok(
                response
        );
    }

    @GetMapping("/flight/{flightScheduleId}")
    public ResponseEntity<List<FlightFareResponse>>
    getFaresByFlightSchedule(
            @PathVariable Long flightScheduleId
    ) {

        List<FlightFareResponse> responses =
                flightFareService
                        .getFaresByFlightSchedule(
                                flightScheduleId
                        );

        return ResponseEntity.ok(
                responses
        );
    }
}