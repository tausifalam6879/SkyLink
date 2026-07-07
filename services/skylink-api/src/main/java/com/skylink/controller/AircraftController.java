package com.skylink.controller;

import com.skylink.dto.AircraftResponse;
import com.skylink.dto.CreateAircraftRequest;
import com.skylink.service.AircraftService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aircraft")
@CrossOrigin("*")
public class AircraftController {

    private final AircraftService aircraftService;

    public AircraftController(
            AircraftService aircraftService
    ) {
        this.aircraftService =
                aircraftService;
    }

    @PostMapping
    public ResponseEntity<AircraftResponse> createAircraft(
            @Valid
            @RequestBody CreateAircraftRequest request
    ) {

        AircraftResponse response =
                aircraftService.createAircraft(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<AircraftResponse>> getAllAircraft() {

        List<AircraftResponse> aircraft =
                aircraftService.getAllAircraft();

        return ResponseEntity.ok(
                aircraft
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<AircraftResponse>> getActiveAircraft() {

        List<AircraftResponse> aircraft =
                aircraftService.getActiveAircraft();

        return ResponseEntity.ok(
                aircraft
        );
    }

    @GetMapping("/{aircraftId}")
    public ResponseEntity<AircraftResponse> getAircraftById(
            @PathVariable Long aircraftId
    ) {

        AircraftResponse response =
                aircraftService.getAircraftById(
                        aircraftId
                );

        return ResponseEntity.ok(
                response
        );
    }

    @GetMapping("/registration/{registrationNumber}")
    public ResponseEntity<AircraftResponse> getAircraftByRegistrationNumber(
            @PathVariable String registrationNumber
    ) {

        AircraftResponse response =
                aircraftService
                        .getAircraftByRegistrationNumber(
                                registrationNumber
                        );

        return ResponseEntity.ok(
                response
        );
    }
}