package com.skylink.importer;

import com.skylink.entity.Airport;
import com.skylink.entity.FlightRoute;
import com.skylink.repository.AirportRepository;
import com.skylink.repository.FlightRouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class FlightRouteDataImporter
        implements CommandLineRunner {

    private static final int BATCH_SIZE = 1000;

    private static final double EARTH_RADIUS_KM =
            6371.0;

    private static final double AVERAGE_FLIGHT_SPEED_KMH =
            800.0;

    private static final int TAXI_AND_BUFFER_MINUTES =
            30;

    private final AirportRepository airportRepository;

    private final FlightRouteRepository flightRouteRepository;

    @Override
    public void run(String... args)
            throws Exception {

        log.info(
                "Starting OpenFlights route data import..."
        );

        long existingRouteCount =
                flightRouteRepository.count();

        log.info(
                "Existing flight routes before import: {}",
                existingRouteCount
        );

        Map<String, Airport> airportsByIataCode =
                loadAirportsByIataCode();

        log.info(
                "Loaded {} airports with valid IATA codes.",
                airportsByIataCode.size()
        );

        Set<String> processedRouteKeys =
                loadExistingRouteKeys();

        log.info(
                "Loaded {} existing route keys.",
                processedRouteKeys.size()
        );

        ClassPathResource resource =
                new ClassPathResource(
                        "data/routes.dat"
                );

        int imported = 0;

        int skippedMissingAirport = 0;

        int skippedInvalidCode = 0;

        int skippedSameAirport = 0;

        int skippedDuplicateRoute = 0;

        int skippedInvalidCoordinates = 0;

        int skippedUnexpectedError = 0;

        List<FlightRoute> batch =
                new ArrayList<>(
                        BATCH_SIZE
                );

        try (
                Reader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        resource.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            Iterable<CSVRecord> records =
                    CSVFormat.DEFAULT
                            .builder()
                            .setIgnoreEmptyLines(true)
                            .setTrim(true)
                            .get()
                            .parse(reader);

            for (CSVRecord record : records) {

                try {

                    if (record.size() < 6) {

                        skippedUnexpectedError++;

                        continue;
                    }

                    String sourceIataCode =
                            normalizeIataCode(
                                    record.get(2)
                            );

                    String destinationIataCode =
                            normalizeIataCode(
                                    record.get(4)
                            );

                    if (
                            sourceIataCode == null
                                    || destinationIataCode == null
                    ) {

                        skippedInvalidCode++;

                        continue;
                    }

                    if (
                            sourceIataCode.equals(
                                    destinationIataCode
                            )
                    ) {

                        skippedSameAirport++;

                        continue;
                    }

                    Airport sourceAirport =
                            airportsByIataCode.get(
                                    sourceIataCode
                            );

                    Airport destinationAirport =
                            airportsByIataCode.get(
                                    destinationIataCode
                            );

                    if (
                            sourceAirport == null
                                    || destinationAirport == null
                    ) {

                        skippedMissingAirport++;

                        continue;
                    }

                    String routeKey =
                            buildRouteKey(
                                    sourceAirport.getId(),
                                    destinationAirport.getId()
                            );

                    if (
                            !processedRouteKeys.add(
                                    routeKey
                            )
                    ) {

                        skippedDuplicateRoute++;

                        continue;
                    }

                    Double distanceKm =
                            calculateDistanceKm(
                                    sourceAirport,
                                    destinationAirport
                            );

                    if (distanceKm == null) {

                        skippedInvalidCoordinates++;

                        continue;
                    }

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
                                            distanceKm
                                    )
                                    .estimatedDurationMinutes(
                                            estimatedDurationMinutes
                                    )
                                    .active(true)
                                    .build();

                    batch.add(
                            flightRoute
                    );

                    if (
                            batch.size()
                                    >= BATCH_SIZE
                    ) {

                        flightRouteRepository.saveAll(
                                batch
                        );

                        flightRouteRepository.flush();

                        imported +=
                                batch.size();

                        log.info(
                                "Imported {} new flight routes...",
                                imported
                        );

                        batch.clear();
                    }

                } catch (Exception exception) {

                    skippedUnexpectedError++;

                    log.debug(
                            "Skipping route record {}: {}",
                            record.getRecordNumber(),
                            exception.getMessage()
                    );
                }
            }

            if (!batch.isEmpty()) {

                flightRouteRepository.saveAll(
                        batch
                );

                flightRouteRepository.flush();

                imported +=
                        batch.size();

                log.info(
                        "Imported {} new flight routes...",
                        imported
                );

                batch.clear();
            }
        }

        log.info(
                "========================================"
        );

        log.info(
                "OpenFlights route import completed."
        );

        log.info(
                "New routes imported: {}",
                imported
        );

        log.info(
                "Skipped - missing airport: {}",
                skippedMissingAirport
        );

        log.info(
                "Skipped - invalid IATA code: {}",
                skippedInvalidCode
        );

        log.info(
                "Skipped - same source/destination: {}",
                skippedSameAirport
        );

        log.info(
                "Skipped - existing/duplicate route: {}",
                skippedDuplicateRoute
        );

        log.info(
                "Skipped - invalid coordinates: {}",
                skippedInvalidCoordinates
        );

        log.info(
                "Skipped - unexpected error: {}",
                skippedUnexpectedError
        );

        log.info(
                "Total routes currently in database: {}",
                flightRouteRepository.count()
        );

        log.info(
                "========================================"
        );
    }

    private Map<String, Airport>
    loadAirportsByIataCode() {

        Map<String, Airport> airportsByIataCode =
                new HashMap<>();

        List<Airport> airports =
                airportRepository.findAll();

        for (Airport airport : airports) {

            String iataCode =
                    normalizeIataCode(
                            airport.getIataCode()
                    );

            if (iataCode == null) {

                continue;
            }

            if (
                    airport.getLatitude() == null
                            || airport.getLongitude() == null
            ) {

                continue;
            }

            airportsByIataCode.putIfAbsent(
                    iataCode,
                    airport
            );
        }

        return airportsByIataCode;
    }

    private Set<String> loadExistingRouteKeys() {

        Set<String> existingRouteKeys =
                new HashSet<>();

        List<FlightRoute> existingRoutes =
                flightRouteRepository.findAll();

        for (
                FlightRoute flightRoute
                : existingRoutes
        ) {

            if (
                    flightRoute.getSourceAirport()
                            == null
                            || flightRoute
                            .getDestinationAirport()
                            == null
            ) {

                continue;
            }

            existingRouteKeys.add(
                    buildRouteKey(
                            flightRoute
                                    .getSourceAirport()
                                    .getId(),
                            flightRoute
                                    .getDestinationAirport()
                                    .getId()
                    )
            );
        }

        return existingRouteKeys;
    }

    private String buildRouteKey(
            Long sourceAirportId,
            Long destinationAirportId
    ) {

        return sourceAirportId
                + "->"
                + destinationAirportId;
    }

    private String normalizeIataCode(
            String value
    ) {

        if (value == null) {

            return null;
        }

        String normalized =
                value
                        .trim()
                        .toUpperCase();

        if (
                normalized.isEmpty()
                        || normalized.equals("\\N")
                        || normalized.length() != 3
        ) {

            return null;
        }

        return normalized;
    }

    private Double calculateDistanceKm(
            Airport sourceAirport,
            Airport destinationAirport
    ) {

        if (
                sourceAirport.getLatitude() == null
                        || sourceAirport.getLongitude() == null
                        || destinationAirport.getLatitude() == null
                        || destinationAirport.getLongitude() == null
        ) {

            return null;
        }

        double sourceLatitude =
                Math.toRadians(
                        sourceAirport.getLatitude()
                );

        double sourceLongitude =
                Math.toRadians(
                        sourceAirport.getLongitude()
                );

        double destinationLatitude =
                Math.toRadians(
                        destinationAirport.getLatitude()
                );

        double destinationLongitude =
                Math.toRadians(
                        destinationAirport.getLongitude()
                );

        double latitudeDifference =
                destinationLatitude
                        - sourceLatitude;

        double longitudeDifference =
                destinationLongitude
                        - sourceLongitude;

        double haversine =
                Math.pow(
                        Math.sin(
                                latitudeDifference / 2
                        ),
                        2
                )
                        + Math.cos(
                        sourceLatitude
                )
                        * Math.cos(
                        destinationLatitude
                )
                        * Math.pow(
                        Math.sin(
                                longitudeDifference / 2
                        ),
                        2
                );

        double angularDistance =
                2
                        * Math.atan2(
                        Math.sqrt(
                                haversine
                        ),
                        Math.sqrt(
                                1 - haversine
                        )
                );

        double distanceKm =
                EARTH_RADIUS_KM
                        * angularDistance;

        return Math.round(
                distanceKm * 100.0
        ) / 100.0;
    }

    private int calculateEstimatedDurationMinutes(
            double distanceKm
    ) {

        double flightHours =
                distanceKm
                        / AVERAGE_FLIGHT_SPEED_KMH;

        int flightMinutes =
                (int) Math.ceil(
                        flightHours * 60
                );

        return Math.max(
                30,
                flightMinutes
                        + TAXI_AND_BUFFER_MINUTES
        );
    }
}