package com.skylink.controller;

import com.skylink.dto.CreateFlightRouteRequest;
import com.skylink.dto.FlightRouteResponse;
import com.skylink.service.FlightRouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flight-routes")
@CrossOrigin("*")
public class FlightRouteController {

    private final FlightRouteService flightRouteService;

    public FlightRouteController(
            FlightRouteService flightRouteService
    ) {
        this.flightRouteService =
                flightRouteService;
    }

    @PostMapping
    public ResponseEntity<FlightRouteResponse> createRoute(
            @Valid
            @RequestBody CreateFlightRouteRequest request
    ) {

        FlightRouteResponse response =
                flightRouteService.createRoute(
                        request.getSourceIataCode(),
                        request.getDestinationIataCode()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/resolve")
    public ResponseEntity<FlightRouteResponse> resolveOrCreateRoute(
            @Valid
            @RequestBody CreateFlightRouteRequest request
    ) {

        FlightRouteResponse response =
                flightRouteService.resolveOrCreateRoute(
                        request.getSourceIataCode(),
                        request.getDestinationIataCode()
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FlightRouteResponse>> getAllRoutes() {

        List<FlightRouteResponse> routes =
                flightRouteService.getAllRoutes();

        return ResponseEntity.ok(routes);
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<FlightRouteResponse> getRouteById(
            @PathVariable Long routeId
    ) {

        FlightRouteResponse response =
                flightRouteService.getRouteById(
                        routeId
                );

        return ResponseEntity.ok(response);
    }
}