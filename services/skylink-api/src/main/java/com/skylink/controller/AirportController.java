package com.skylink.controller;

import com.skylink.dto.AirportResponse;
import com.skylink.dto.NearbyAirportResponse;
import com.skylink.service.AirportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
@CrossOrigin("*")
public class AirportController {

    private final AirportService airportService;

    public AirportController(
            AirportService airportService
    ) {
        this.airportService = airportService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<AirportResponse>> searchAirports(
            @RequestParam String query
    ) {

        return ResponseEntity.ok(
                airportService.searchAirports(query)
        );
    }

    @GetMapping("/iata/{iataCode}")
    public ResponseEntity<AirportResponse> getAirportByIataCode(
            @PathVariable String iataCode
    ) {

        return ResponseEntity.ok(
                airportService.getAirportByIataCode(
                        iataCode
                )
        );
    }

    @GetMapping("/icao/{icaoCode}")
    public ResponseEntity<AirportResponse> getAirportByIcaoCode(
            @PathVariable String icaoCode
    ) {

        return ResponseEntity.ok(
                airportService.getAirportByIcaoCode(
                        icaoCode
                )
        );
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyAirportResponse>> getNearbyAirports(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Integer limit
    ) {

        return ResponseEntity.ok(
                airportService.getNearbyAirports(
                        latitude,
                        longitude,
                        limit
                )
        );
    }
}