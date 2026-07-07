package com.skylink.importer;

import com.skylink.entity.Aircraft;
import com.skylink.entity.FareClass;
import com.skylink.entity.FlightFare;
import com.skylink.entity.FlightRoute;
import com.skylink.entity.FlightSchedule;
import com.skylink.entity.FlightStatus;
import com.skylink.repository.AircraftRepository;
import com.skylink.repository.FlightFareRepository;
import com.skylink.repository.FlightRouteRepository;
import com.skylink.repository.FlightScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(4)
public class FlightScheduleDataGenerator
        implements CommandLineRunner {

    /*
     * Startup par sirf small seed window generate hoga.
     *
     * Large/global route support FlightService ke
     * on-demand schedule generation se handle hoga.
     */
    private static final int STARTUP_SEED_DAYS = 7;

    /*
     * Startup par poori 300 aircraft fleet ko schedule
     * generate karne ki zarurat nahi hai.
     */
    private static final int MAX_STARTUP_AIRCRAFT = 12;

    /*
     * CSV-backed routes database me thousands ho sakte hain.
     * Startup seed ke liye limited routes enough hain.
     */
    private static final int MAX_STARTUP_ROUTES = 120;

    private final FlightRouteRepository
            flightRouteRepository;

    private final AircraftRepository
            aircraftRepository;

    private final FlightScheduleRepository
            flightScheduleRepository;

    private final FlightFareRepository
            flightFareRepository;

    @Override
    @Transactional
    public void run(String... args) {

        log.info(
                "Starting SkyLink lightweight startup schedule seed..."
        );

        LocalDate startDate =
                LocalDate.now();

        LocalDate endDate =
                startDate.plusDays(
                        STARTUP_SEED_DAYS - 1
                );

        LocalDateTime startDateTime =
                startDate.atStartOfDay();

        LocalDateTime endDateTime =
                endDate.atTime(
                        LocalTime.MAX
                );

        long existingSchedules =
                flightScheduleRepository
                        .countByDepartureTimeBetween(
                                startDateTime,
                                endDateTime
                        );

        log.info(
                "Existing schedules between {} and {}: {}",
                startDate,
                endDate,
                existingSchedules
        );

        /*
         * Important:
         *
         * Agar startup seed window me already schedules hain,
         * to generator dobara unnecessary thousands of SELECT
         * queries nahi chalayega.
         */
        if (existingSchedules > 0) {

            log.info(
                    "Startup schedule seed skipped because schedules already exist."
            );

            log.info(
                    "Global route/date schedules will continue to be generated on demand."
            );

            return;
        }

        List<Aircraft> aircraftList =
                aircraftRepository
                        .findByActiveTrue()
                        .stream()
                        .filter(
                                aircraft ->
                                        aircraft
                                                .getRegistrationNumber()
                                                != null
                                                &&
                                                aircraft
                                                        .getRegistrationNumber()
                                                        .startsWith(
                                                                "VT-SLA"
                                                        )
                        )
                        .limit(
                                MAX_STARTUP_AIRCRAFT
                        )
                        .toList();

        if (aircraftList.isEmpty()) {

            log.warn(
                    "No active SkyLink aircraft found."
            );

            return;
        }

        List<FlightRoute> routes =
                flightRouteRepository
                        .findByActiveTrue()
                        .stream()
                        .filter(
                                this::isValidRoute
                        )
                        .limit(
                                MAX_STARTUP_ROUTES
                        )
                        .toList();

        if (routes.isEmpty()) {

            log.warn(
                    "No valid CSV-backed active flight routes found."
            );

            return;
        }

        log.info(
                "Startup aircraft selected: {}",
                aircraftList.size()
        );

        log.info(
                "Startup CSV-backed routes selected: {}",
                routes.size()
        );

        log.info(
                "Startup seed days: {}",
                STARTUP_SEED_DAYS
        );

        log.info(
                "Large/global route coverage is handled by on-demand schedule generation."
        );

        int generatedSchedules = 0;

        int skippedSchedules = 0;

        int generatedFares = 0;

        for (
                int dayOffset = 0;
                dayOffset < STARTUP_SEED_DAYS;
                dayOffset++
        ) {

            LocalDate scheduleDate =
                    startDate.plusDays(
                            dayOffset
                    );

            for (
                    int aircraftIndex = 0;
                    aircraftIndex < aircraftList.size();
                    aircraftIndex++
            ) {

                Aircraft aircraft =
                        aircraftList.get(
                                aircraftIndex
                        );

                int routeIndex =
                        Math.floorMod(
                                dayOffset
                                        * aircraftList.size()
                                        + aircraftIndex,
                                routes.size()
                        );

                FlightRoute route =
                        routes.get(
                                routeIndex
                        );

                LocalTime departureSlot =
                        resolveDepartureSlot(
                                aircraftIndex
                        );

                LocalDateTime departureTime =
                        scheduleDate.atTime(
                                departureSlot
                        );

                int durationMinutes =
                        resolveDurationMinutes(
                                route
                        );

                LocalDateTime arrivalTime =
                        departureTime.plusMinutes(
                                durationMinutes
                        );

                boolean aircraftBusy =
                        flightScheduleRepository
                                .existsByAircraftAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
                                        aircraft,
                                        arrivalTime,
                                        departureTime
                                );

                if (aircraftBusy) {

                    skippedSchedules++;

                    continue;
                }

                boolean routeScheduleExists =
                        flightScheduleRepository
                                .existsByFlightRouteAndDepartureTime(
                                        route,
                                        departureTime
                                );

                if (routeScheduleExists) {

                    skippedSchedules++;

                    continue;
                }

                String flightNumber =
                        generateFlightNumber(
                                scheduleDate,
                                aircraftIndex,
                                routeIndex
                        );

                FlightSchedule flightSchedule =
                        FlightSchedule.builder()
                                .flightNumber(
                                        flightNumber
                                )
                                .flightRoute(
                                        route
                                )
                                .aircraft(
                                        aircraft
                                )
                                .departureTime(
                                        departureTime
                                )
                                .arrivalTime(
                                        arrivalTime
                                )
                                .status(
                                        FlightStatus.SCHEDULED
                                )
                                .active(true)
                                .build();

                FlightSchedule savedSchedule =
                        flightScheduleRepository.save(
                                flightSchedule
                        );

                generatedSchedules++;

                generatedFares +=
                        createFlightFares(
                                savedSchedule,
                                aircraft,
                                route
                        );
            }
        }

        log.info(
                "========================================"
        );

        log.info(
                "SkyLink lightweight startup schedule seed completed."
        );

        log.info(
                "Generated startup schedules: {}",
                generatedSchedules
        );

        log.info(
                "Skipped startup schedules: {}",
                skippedSchedules
        );

        log.info(
                "Generated startup fares: {}",
                generatedFares
        );

        log.info(
                "Startup seed date range: {} to {}",
                startDate,
                endDate
        );

        log.info(
                "Global CSV-backed routes remain available for on-demand search."
        );

        log.info(
                "Total schedules currently in database: {}",
                flightScheduleRepository.count()
        );

        log.info(
                "Total fares currently in database: {}",
                flightFareRepository.count()
        );

        log.info(
                "========================================"
        );
    }

    private boolean isValidRoute(
            FlightRoute route
    ) {

        if (
                route == null
                        ||
                        route.getSourceAirport() == null
                        ||
                        route.getDestinationAirport() == null
        ) {

            return false;
        }

        String sourceIataCode =
                route
                        .getSourceAirport()
                        .getIataCode();

        String destinationIataCode =
                route
                        .getDestinationAirport()
                        .getIataCode();

        return sourceIataCode != null
                &&
                !sourceIataCode.isBlank()
                &&
                destinationIataCode != null
                &&
                !destinationIataCode.isBlank()
                &&
                !sourceIataCode.equalsIgnoreCase(
                        destinationIataCode
                );
    }

    private LocalTime resolveDepartureSlot(
            int aircraftIndex
    ) {

        LocalTime[] departureSlots = {
                LocalTime.of(6, 0),
                LocalTime.of(7, 30),
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                LocalTime.of(12, 0),
                LocalTime.of(13, 30),
                LocalTime.of(15, 0),
                LocalTime.of(16, 30),
                LocalTime.of(18, 0),
                LocalTime.of(19, 30),
                LocalTime.of(21, 0),
                LocalTime.of(22, 30)
        };

        return departureSlots[
                Math.floorMod(
                        aircraftIndex,
                        departureSlots.length
                )
                ];
    }

    private int resolveDurationMinutes(
            FlightRoute route
    ) {

        if (
                route.getEstimatedDurationMinutes()
                        != null
                        &&
                        route.getEstimatedDurationMinutes()
                                > 0
        ) {

            return route
                    .getEstimatedDurationMinutes();
        }

        double distanceKm =
                route.getDistanceKm() != null
                        ? route.getDistanceKm()
                        : 500.0;

        double estimatedHours =
                distanceKm / 750.0;

        int estimatedMinutes =
                (int) Math.ceil(
                        estimatedHours * 60.0
                );

        return Math.max(
                estimatedMinutes + 30,
                60
        );
    }

    private int createFlightFares(
            FlightSchedule flightSchedule,
            Aircraft aircraft,
            FlightRoute route
    ) {

        int generatedFares = 0;

        BigDecimal economyFare =
                calculateEconomyFare(
                        route
                );

        if (
                aircraft.getEconomySeats() != null
                        &&
                        aircraft.getEconomySeats() > 0
        ) {

            generatedFares +=
                    saveFare(
                            flightSchedule,
                            FareClass.ECONOMY,
                            economyFare,
                            aircraft.getEconomySeats()
                    );
        }

        if (
                aircraft.getBusinessSeats() != null
                        &&
                        aircraft.getBusinessSeats() > 0
        ) {

            generatedFares +=
                    saveFare(
                            flightSchedule,
                            FareClass.BUSINESS,
                            economyFare.multiply(
                                    BigDecimal.valueOf(
                                            2.35
                                    )
                            ),
                            aircraft.getBusinessSeats()
                    );
        }

        if (
                aircraft.getFirstClassSeats() != null
                        &&
                        aircraft.getFirstClassSeats() > 0
        ) {

            generatedFares +=
                    saveFare(
                            flightSchedule,
                            FareClass.FIRST_CLASS,
                            economyFare.multiply(
                                    BigDecimal.valueOf(
                                            4.50
                                    )
                            ),
                            aircraft.getFirstClassSeats()
                    );
        }

        return generatedFares;
    }

    private int saveFare(
            FlightSchedule flightSchedule,
            FareClass fareClass,
            BigDecimal baseFare,
            Integer availableSeats
    ) {

        boolean fareExists =
                flightFareRepository
                        .existsByFlightScheduleAndFareClass(
                                flightSchedule,
                                fareClass
                        );

        if (fareExists) {

            return 0;
        }

        FlightFare flightFare =
                FlightFare.builder()
                        .flightSchedule(
                                flightSchedule
                        )
                        .fareClass(
                                fareClass
                        )
                        .baseFare(
                                baseFare.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        )
                        .availableSeats(
                                availableSeats
                        )
                        .active(true)
                        .build();

        flightFareRepository.save(
                flightFare
        );

        return 1;
    }

    private BigDecimal calculateEconomyFare(
            FlightRoute route
    ) {

        double distanceKm =
                route.getDistanceKm() != null
                        ? route.getDistanceKm()
                        : 500.0;

        double calculatedFare =
                1500.0
                        + (
                        distanceKm * 4.25
                );

        calculatedFare =
                Math.max(
                        calculatedFare,
                        2200.0
                );

        return BigDecimal
                .valueOf(
                        calculatedFare
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private String generateFlightNumber(
            LocalDate scheduleDate,
            int aircraftIndex,
            int routeIndex
    ) {

        long seed =
                scheduleDate.toEpochDay()
                        * 31L
                        + aircraftIndex * 17L
                        + routeIndex * 13L;

        int numericFlightNumber =
                1000
                        + Math.floorMod(
                        seed,
                        8999
                );

        return "SL"
                + numericFlightNumber;
    }
}