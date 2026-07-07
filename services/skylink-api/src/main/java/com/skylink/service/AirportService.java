package com.skylink.service;

import com.skylink.dto.AirportResponse;
import com.skylink.dto.NearbyAirportResponse;
import com.skylink.entity.Airport;
import com.skylink.repository.AirportRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AirportService {

    private static final int SEARCH_RESULT_LIMIT = 20;

    private static final int DEFAULT_NEARBY_LIMIT = 10;

    private static final int MAX_NEARBY_LIMIT = 50;

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final AirportRepository airportRepository;

    public AirportService(
            AirportRepository airportRepository
    ) {
        this.airportRepository = airportRepository;
    }

    public List<AirportResponse> searchAirports(
            String keyword
    ) {

        if (keyword == null || keyword.isBlank()) {

            throw new RuntimeException(
                    "Airport search keyword is required."
            );
        }

        String normalizedKeyword =
                keyword.trim();

        return airportRepository
                .searchAirports(
                        normalizedKeyword,
                        PageRequest.of(
                                0,
                                SEARCH_RESULT_LIMIT
                        )
                )
                .stream()
                .map(this::mapToAirportResponse)
                .toList();
    }

    public AirportResponse getAirportByIataCode(
            String iataCode
    ) {

        if (iataCode == null || iataCode.isBlank()) {

            throw new RuntimeException(
                    "IATA code is required."
            );
        }

        String normalizedIataCode =
                iataCode.trim().toUpperCase();

        Airport airport = airportRepository
                .findByIataCodeIgnoreCase(
                        normalizedIataCode
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "Airport not found for IATA code: "
                                        + normalizedIataCode
                        )
                );

        return mapToAirportResponse(airport);
    }

    public AirportResponse getAirportByIcaoCode(
            String icaoCode
    ) {

        if (icaoCode == null || icaoCode.isBlank()) {

            throw new RuntimeException(
                    "ICAO code is required."
            );
        }

        String normalizedIcaoCode =
                icaoCode.trim().toUpperCase();

        Airport airport = airportRepository
                .findByIcaoCodeIgnoreCase(
                        normalizedIcaoCode
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "Airport not found for ICAO code: "
                                        + normalizedIcaoCode
                        )
                );

        return mapToAirportResponse(airport);
    }

    public List<NearbyAirportResponse> getNearbyAirports(
            Double latitude,
            Double longitude,
            Integer limit
    ) {

        validateCoordinates(
                latitude,
                longitude
        );

        int normalizedLimit =
                normalizeNearbyLimit(limit);

        return airportRepository
                .findNearbyAirports(
                        latitude,
                        longitude,
                        normalizedLimit
                )
                .stream()
                .map(
                        airport ->
                                mapToNearbyAirportResponse(
                                        airport,
                                        latitude,
                                        longitude
                                )
                )
                .toList();
    }

    private void validateCoordinates(
            Double latitude,
            Double longitude
    ) {

        if (latitude == null) {

            throw new RuntimeException(
                    "Latitude is required."
            );
        }

        if (longitude == null) {

            throw new RuntimeException(
                    "Longitude is required."
            );
        }

        if (latitude < -90.0 || latitude > 90.0) {

            throw new RuntimeException(
                    "Latitude must be between -90 and 90."
            );
        }

        if (longitude < -180.0 || longitude > 180.0) {

            throw new RuntimeException(
                    "Longitude must be between -180 and 180."
            );
        }
    }

    private int normalizeNearbyLimit(
            Integer limit
    ) {

        if (limit == null) {

            return DEFAULT_NEARBY_LIMIT;
        }

        if (limit < 1) {

            throw new RuntimeException(
                    "Nearby airport limit must be at least 1."
            );
        }

        if (limit > MAX_NEARBY_LIMIT) {

            throw new RuntimeException(
                    "Nearby airport limit cannot exceed 50."
            );
        }

        return limit;
    }

    private AirportResponse mapToAirportResponse(
            Airport airport
    ) {

        return AirportResponse.builder()
                .id(airport.getId())
                .iataCode(airport.getIataCode())
                .icaoCode(airport.getIcaoCode())
                .airportName(airport.getAirportName())
                .city(airport.getCity())
                .countryCode(airport.getCountryCode())
                .airportType(airport.getAirportType())
                .latitude(airport.getLatitude())
                .longitude(airport.getLongitude())
                .elevation(airport.getElevation())
                .build();
    }

    private NearbyAirportResponse mapToNearbyAirportResponse(
            Airport airport,
            Double userLatitude,
            Double userLongitude
    ) {

        double distanceKm =
                calculateDistanceKm(
                        userLatitude,
                        userLongitude,
                        airport.getLatitude(),
                        airport.getLongitude()
                );

        return NearbyAirportResponse.builder()
                .id(airport.getId())
                .iataCode(airport.getIataCode())
                .icaoCode(airport.getIcaoCode())
                .airportName(airport.getAirportName())
                .city(airport.getCity())
                .countryCode(airport.getCountryCode())
                .airportType(airport.getAirportType())
                .latitude(airport.getLatitude())
                .longitude(airport.getLongitude())
                .elevation(airport.getElevation())
                .distanceKm(
                        Math.round(
                                distanceKm * 100.0
                        ) / 100.0
                )
                .build();
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
}