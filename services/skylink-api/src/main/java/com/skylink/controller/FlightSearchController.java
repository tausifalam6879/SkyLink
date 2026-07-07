package com.skylink.controller;

import com.skylink.dto.FlightSearchRequest;
import com.skylink.dto.FlightSearchResponse;
import com.skylink.service.FlightSearchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@CrossOrigin("*")
public class FlightSearchController {

    private final FlightSearchService flightSearchService;

    public FlightSearchController(
            FlightSearchService flightSearchService
    ) {
        this.flightSearchService =
                flightSearchService;
    }

    @PostMapping("/search")
    public ResponseEntity<List<FlightSearchResponse>> searchFlights(
            @Valid
            @RequestBody FlightSearchRequest request
    ) {

        List<FlightSearchResponse> responses =
                flightSearchService.searchFlights(
                        request
                );

        return ResponseEntity.ok(
                responses
        );
    }
}