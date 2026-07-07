package com.skylink.service;

import com.skylink.dto.FlightRouteResponse;
import com.skylink.entity.Airport;
import com.skylink.entity.FlightRoute;
import com.skylink.repository.AirportRepository;
import com.skylink.repository.FlightRouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlightRouteService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private static final double AVERAGE_FLIGHT_SPEED_KMH = 800.0;

    private static final int FLIGHT_OVERHEAD_MINUTES = 30;

    private final FlightRouteRepository flightRouteRepository;

    private final AirportRepository airportRepository;

    public FlightRouteService(
            FlightRouteRepository flightRouteRepository,
            AirportRepository airportRepository
    ) {
        this.flightRouteRepository =
                flightRouteRepository;

        this.airportRepository =
                airportRepository;
    }

    @Transactional
    public FlightRouteResponse createRoute(
            String sourceIataCode,
            String destinationIataCode
    ) {

        if (sourceIataCode == null
                || sourceIataCode.isBlank()) {

            throw new RuntimeException(
                    "Source IATA code is required."
            );
        }

        if (destinationIataCode == null
                || destinationIataCode.isBlank()) {

            throw new RuntimeException(
                    "Destination IATA code is required."
            );
        }

        String normalizedSourceCode =
                sourceIataCode
                        .trim()
                        .toUpperCase();

        String normalizedDestinationCode =
                destinationIataCode
                        .trim()
                        .toUpperCase();

        if (normalizedSourceCode.equals(
                normalizedDestinationCode
        )) {

            throw new RuntimeException(
                    "Source and destination airports cannot be the same."
            );
        }

        Airport sourceAirport =
                airportRepository
                        .findByIataCodeIgnoreCase(
                                normalizedSourceCode
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Source airport not found: "
                                                + normalizedSourceCode
                                )
                        );

        Airport destinationAirport =
                airportRepository
                        .findByIataCodeIgnoreCase(
                                normalizedDestinationCode
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Destination airport not found: "
                                                + normalizedDestinationCode
                                )
                        );

        validateAirportCoordinates(
                sourceAirport,
                "Source"
        );

        validateAirportCoordinates(
                destinationAirport,
                "Destination"
        );

        if (flightRouteRepository
                .existsBySourceAirportAndDestinationAirport(
                        sourceAirport,
                        destinationAirport
                )) {

            throw new RuntimeException(
                    "Flight route already exists: "
                            + normalizedSourceCode
                            + " -> "
                            + normalizedDestinationCode
            );
        }

        double distanceKm =
                calculateDistanceKm(
                        sourceAirport.getLatitude(),
                        sourceAirport.getLongitude(),
                        destinationAirport.getLatitude(),
                        destinationAirport.getLongitude()
                );

        int estimatedDurationMinutes =
                calculateEstimatedDurationMinutes(
                        distanceKm
                );

        FlightRoute flightRoute =
                FlightRoute.builder()
                        .sourceAirport(
                                sourceAirport
                        )
                        .destinationAirport(
                                destinationAirport
                        )
                        .distanceKm(
                                roundDistance(distanceKm)
                        )
                        .estimatedDurationMinutes(
                                estimatedDurationMinutes
                        )
                        .active(true)
                        .build();

        FlightRoute savedRoute =
                flightRouteRepository.save(
                        flightRoute
                );

        return mapToFlightRouteResponse(
                savedRoute
        );
    }

    @Transactional(readOnly = true)
    public List<FlightRouteResponse> getAllRoutes() {

        return flightRouteRepository
                .findAll()
                .stream()
                .map(this::mapToFlightRouteResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FlightRouteResponse getRouteById(
            Long routeId
    ) {

        if (routeId == null) {

            throw new RuntimeException(
                    "Flight route ID is required."
            );
        }

        FlightRoute flightRoute =
                flightRouteRepository
                        .findById(routeId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Flight route not found with ID: "
                                                + routeId
                                )
                        );

        return mapToFlightRouteResponse(
                flightRoute
        );
    }

    @Transactional
    public FlightRouteResponse resolveOrCreateRoute(
            String sourceIataCode,
            String destinationIataCode
    ) {

        if (sourceIataCode == null
                || sourceIataCode.isBlank()) {

            throw new RuntimeException(
                    "Source IATA code is required."
            );
        }

        if (destinationIataCode == null
                || destinationIataCode.isBlank()) {

            throw new RuntimeException(
                    "Destination IATA code is required."
            );
        }

        String normalizedSourceCode =
                sourceIataCode
                        .trim()
                        .toUpperCase();

        String normalizedDestinationCode =
                destinationIataCode
                        .trim()
                        .toUpperCase();

        if (normalizedSourceCode.equals(
                normalizedDestinationCode
        )) {

            throw new RuntimeException(
                    "Source and destination airports cannot be the same."
            );
        }

        Airport sourceAirport =
                airportRepository
                        .findByIataCodeIgnoreCase(
                                normalizedSourceCode
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Source airport not found: "
                                                + normalizedSourceCode
                                )
                        );

        Airport destinationAirport =
                airportRepository
                        .findByIataCodeIgnoreCase(
                                normalizedDestinationCode
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Destination airport not found: "
                                                + normalizedDestinationCode
                                )
                        );

        validateAirportCoordinates(
                sourceAirport,
                "Source"
        );

        validateAirportCoordinates(
                destinationAirport,
                "Destination"
        );

        return flightRouteRepository
                .findBySourceAirportAndDestinationAirport(
                        sourceAirport,
                        destinationAirport
                )
                .map(this::mapToFlightRouteResponse)
                .orElseGet(() -> {

                    double distanceKm =
                            calculateDistanceKm(
                                    sourceAirport.getLatitude(),
                                    sourceAirport.getLongitude(),
                                    destinationAirport.getLatitude(),
                                    destinationAirport.getLongitude()
                            );

                    int estimatedDurationMinutes =
                            calculateEstimatedDurationMinutes(
                                    distanceKm
                            );

                    FlightRoute flightRoute =
                            FlightRoute.builder()
                                    .sourceAirport(
                                            sourceAirport
                                    )
                                    .destinationAirport(
                                            destinationAirport
                                    )
                                    .distanceKm(
                                            roundDistance(
                                                    distanceKm
                                            )
                                    )
                                    .estimatedDurationMinutes(
                                            estimatedDurationMinutes
                                    )
                                    .active(true)
                                    .build();

                    FlightRoute savedRoute =
                            flightRouteRepository.save(
                                    flightRoute
                            );

                    return mapToFlightRouteResponse(
                            savedRoute
                    );
                });
    }

    private void validateAirportCoordinates(
            Airport airport,
            String airportRole
    ) {

        if (airport.getLatitude() == null
                || airport.getLongitude() == null) {

            throw new RuntimeException(
                    airportRole
                            + " airport coordinates are unavailable."
            );
        }
    }

    private int calculateEstimatedDurationMinutes(
            double distanceKm
    ) {

        double flightHours =
                distanceKm
                        / AVERAGE_FLIGHT_SPEED_KMH;

        int flightMinutes =
                (int) Math.ceil(
                        flightHours * 60.0
                );

        return flightMinutes
                + FLIGHT_OVERHEAD_MINUTES;
    }

    private double calculateDistanceKm(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2
    ) {

        double latitudeDistance =
                Math.toRadians(
                        latitude2 - latitude1
                );

        double longitudeDistance =
                Math.toRadians(
                        longitude2 - longitude1
                );

        double haversine =
                Math.sin(latitudeDistance / 2)
                        * Math.sin(latitudeDistance / 2)
                        + Math.cos(
                        Math.toRadians(latitude1)
                )
                        * Math.cos(
                        Math.toRadians(latitude2)
                )
                        * Math.sin(longitudeDistance / 2)
                        * Math.sin(longitudeDistance / 2);

        double angularDistance =
                2 * Math.atan2(
                        Math.sqrt(haversine),
                        Math.sqrt(1 - haversine)
                );

        return EARTH_RADIUS_KM
                * angularDistance;
    }

    private double roundDistance(
            double distanceKm
    ) {

        return Math.round(
                distanceKm * 100.0
        ) / 100.0;
    }

    private FlightRouteResponse mapToFlightRouteResponse(
            FlightRoute flightRoute
    ) {

        Airport sourceAirport =
                flightRoute.getSourceAirport();

        Airport destinationAirport =
                flightRoute.getDestinationAirport();

        return FlightRouteResponse.builder()
                .id(
                        flightRoute.getId()
                )
                .sourceAirportId(
                        sourceAirport.getId()
                )
                .sourceIataCode(
                        sourceAirport.getIataCode()
                )
                .sourceAirportName(
                        sourceAirport.getAirportName()
                )
                .sourceCity(
                        sourceAirport.getCity()
                )
                .sourceCountryCode(
                        sourceAirport.getCountryCode()
                )
                .destinationAirportId(
                        destinationAirport.getId()
                )
                .destinationIataCode(
                        destinationAirport.getIataCode()
                )
                .destinationAirportName(
                        destinationAirport.getAirportName()
                )
                .destinationCity(
                        destinationAirport.getCity()
                )
                .destinationCountryCode(
                        destinationAirport.getCountryCode()
                )
                .distanceKm(
                        flightRoute.getDistanceKm()
                )
                .estimatedDurationMinutes(
                        flightRoute.getEstimatedDurationMinutes()
                )
                .active(
                        flightRoute.isActive()
                )
                .build();
    }
}