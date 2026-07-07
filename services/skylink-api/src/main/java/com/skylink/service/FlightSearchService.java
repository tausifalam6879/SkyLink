package com.skylink.service;

import com.skylink.dto.FlightFareResponse;
import com.skylink.dto.FlightSearchRequest;
import com.skylink.dto.FlightSearchResponse;
import com.skylink.entity.Aircraft;
import com.skylink.entity.Airport;
import com.skylink.entity.FareClass;
import com.skylink.entity.FlightFare;
import com.skylink.entity.FlightRoute;
import com.skylink.entity.FlightSchedule;
import com.skylink.entity.FlightStatus;
import com.skylink.importer.SeatDataGenerator;
import com.skylink.repository.AircraftRepository;
import com.skylink.repository.FlightFareRepository;
import com.skylink.repository.FlightRouteRepository;
import com.skylink.repository.FlightScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FlightSearchService {

    private final FlightScheduleRepository
            flightScheduleRepository;

    private final FlightFareRepository
            flightFareRepository;

    private final FlightRouteRepository
            flightRouteRepository;

    private final AircraftRepository
            aircraftRepository;

    private final FlightRouteService
            flightRouteService;

    private final SeatDataGenerator
            seatDataGenerator;

    public FlightSearchService(
            FlightScheduleRepository flightScheduleRepository,
            FlightFareRepository flightFareRepository,
            FlightRouteRepository flightRouteRepository,
            AircraftRepository aircraftRepository,
            FlightRouteService flightRouteService,
            SeatDataGenerator seatDataGenerator
    ) {

        this.flightScheduleRepository =
                flightScheduleRepository;

        this.flightFareRepository =
                flightFareRepository;

        this.flightRouteRepository =
                flightRouteRepository;

        this.aircraftRepository =
                aircraftRepository;

        this.flightRouteService =
                flightRouteService;

        this.seatDataGenerator =
                seatDataGenerator;
    }

    @Transactional
    public List<FlightSearchResponse> searchFlights(
            FlightSearchRequest request
    ) {

        validateSearchRequest(
                request
        );

        String sourceIataCode =
                normalizeIataCode(
                        request.getSourceIataCode()
                );

        String destinationIataCode =
                normalizeIataCode(
                        request.getDestinationIataCode()
                );

        if (
                sourceIataCode.equals(
                        destinationIataCode
                )
        ) {

            throw new RuntimeException(
                    "Source and destination airports cannot be the same."
            );
        }

        LocalDate travelDate =
                request.getTravelDate();

        if (
                travelDate.isBefore(
                        LocalDate.now()
                )
        ) {

            throw new RuntimeException(
                    "Travel date cannot be in the past."
            );
        }

        LocalDateTime startDateTime =
                travelDate.atStartOfDay();

        LocalDateTime endDateTime =
                travelDate.atTime(
                        LocalTime.MAX
                );

        List<FlightSchedule> flightSchedules =
                findScheduledFlights(
                        sourceIataCode,
                        destinationIataCode,
                        startDateTime,
                        endDateTime
                );

        if (
                flightSchedules.isEmpty()
        ) {

            FlightRoute flightRoute =
                    resolveFlightRoute(
                            sourceIataCode,
                            destinationIataCode
                    );

            generateMissingSchedulesForRouteAndDate(
                    flightRoute,
                    travelDate
            );

            flightSchedules =
                    findScheduledFlights(
                            sourceIataCode,
                            destinationIataCode,
                            startDateTime,
                            endDateTime
                    );
        }

        return flightSchedules
                .stream()
                .map(
                        this::mapToFlightSearchResponse
                )
                .filter(
                        response ->
                                response.getFares() != null
                                        &&
                                        !response
                                                .getFares()
                                                .isEmpty()
                )
                .toList();
    }

    private void validateSearchRequest(
            FlightSearchRequest request
    ) {

        if (
                request == null
        ) {

            throw new RuntimeException(
                    "Flight search request is required."
            );
        }

        if (
                request.getSourceIataCode() == null
                        ||
                        request
                                .getSourceIataCode()
                                .isBlank()
        ) {

            throw new RuntimeException(
                    "Source IATA code is required."
            );
        }

        if (
                request.getDestinationIataCode() == null
                        ||
                        request
                                .getDestinationIataCode()
                                .isBlank()
        ) {

            throw new RuntimeException(
                    "Destination IATA code is required."
            );
        }

        if (
                request.getTravelDate() == null
        ) {

            throw new RuntimeException(
                    "Travel date is required."
            );
        }
    }

    private String normalizeIataCode(
            String iataCode
    ) {

        return iataCode
                .trim()
                .toUpperCase();
    }

    private List<FlightSchedule>
    findScheduledFlights(
            String sourceIataCode,
            String destinationIataCode,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {

        return flightScheduleRepository
                .findByFlightRouteSourceAirportIataCodeIgnoreCaseAndFlightRouteDestinationAirportIataCodeIgnoreCaseAndDepartureTimeBetweenAndStatusAndActiveTrueOrderByDepartureTimeAsc(
                        sourceIataCode,
                        destinationIataCode,
                        startDateTime,
                        endDateTime,
                        FlightStatus.SCHEDULED
                );
    }

    private FlightRoute resolveFlightRoute(
            String sourceIataCode,
            String destinationIataCode
    ) {

        return flightRouteRepository
                .findBySourceAirportIataCodeIgnoreCaseAndDestinationAirportIataCodeIgnoreCase(
                        sourceIataCode,
                        destinationIataCode
                )
                .orElseGet(
                        () -> {

                            Long flightRouteId =
                                    flightRouteService
                                            .resolveOrCreateRoute(
                                                    sourceIataCode,
                                                    destinationIataCode
                                            )
                                            .getId();

                            return flightRouteRepository
                                    .findById(
                                            flightRouteId
                                    )
                                    .orElseThrow(
                                            () ->
                                                    new RuntimeException(
                                                            "Flight route could not be resolved."
                                                    )
                                    );
                        }
                );
    }

    private void generateMissingSchedulesForRouteAndDate(
            FlightRoute flightRoute,
            LocalDate travelDate
    ) {

        LocalDateTime startDateTime =
                travelDate.atStartOfDay();

        LocalDateTime endDateTime =
                travelDate.atTime(
                        LocalTime.MAX
                );

        long existingRouteSchedules =
                flightScheduleRepository
                        .countByFlightRouteAndDepartureTimeBetween(
                                flightRoute,
                                startDateTime,
                                endDateTime
                        );

        if (
                existingRouteSchedules > 0
        ) {

            return;
        }

        List<Aircraft> activeAircraft =
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
                        .toList();

        if (
                activeAircraft.isEmpty()
        ) {

            throw new RuntimeException(
                    "No active SkyLink aircraft are available."
            );
        }

        int scheduleFrequency =
                calculateScheduleFrequency(
                        flightRoute
                );

        List<LocalTime> departureTimes =
                buildDepartureTimes(
                        scheduleFrequency
                );

        int createdSchedules = 0;

        for (
                LocalTime departureLocalTime
                : departureTimes
        ) {

            LocalDateTime departureTime =
                    travelDate.atTime(
                            departureLocalTime
                    );

            if (
                    !departureTime.isAfter(
                            LocalDateTime.now()
                    )
            ) {

                continue;
            }

            int durationMinutes =
                    resolveDurationMinutes(
                            flightRoute
                    );

            LocalDateTime arrivalTime =
                    departureTime.plusMinutes(
                            durationMinutes
                    );

            Aircraft availableAircraft =
                    findAvailableAircraft(
                            activeAircraft,
                            departureTime,
                            arrivalTime
                    );

            if (
                    availableAircraft == null
            ) {

                continue;
            }

            boolean scheduleExists =
                    flightScheduleRepository
                            .existsByFlightRouteAndDepartureTime(
                                    flightRoute,
                                    departureTime
                            );

            if (
                    scheduleExists
            ) {

                continue;
            }

            FlightSchedule flightSchedule =
                    FlightSchedule.builder()
                            .flightNumber(
                                    generateFlightNumber()
                            )
                            .flightRoute(
                                    flightRoute
                            )
                            .aircraft(
                                    availableAircraft
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

            FlightSchedule savedFlightSchedule =
                    flightScheduleRepository.save(
                            flightSchedule
                    );

            createFlightFares(
                    savedFlightSchedule,
                    availableAircraft,
                    flightRoute
            );

            seatDataGenerator
                    .generateSeatMapForSchedule(
                            savedFlightSchedule
                    );

            createdSchedules++;
        }

        if (
                createdSchedules == 0
        ) {

            throw new RuntimeException(
                    "No SkyLink aircraft are available for "
                            + flightRoute
                            .getSourceAirport()
                            .getIataCode()
                            + " to "
                            + flightRoute
                            .getDestinationAirport()
                            .getIataCode()
                            + " on "
                            + travelDate
                            + "."
            );
        }
    }

    private int calculateScheduleFrequency(
            FlightRoute flightRoute
    ) {

        double distanceKm =
                flightRoute.getDistanceKm() != null
                        ? flightRoute.getDistanceKm()
                        : 500.0;

        Airport sourceAirport =
                flightRoute.getSourceAirport();

        Airport destinationAirport =
                flightRoute.getDestinationAirport();

        boolean domesticRoute =
                sourceAirport.getCountryCode() != null
                        &&
                        destinationAirport.getCountryCode() != null
                        &&
                        sourceAirport
                                .getCountryCode()
                                .equalsIgnoreCase(
                                        destinationAirport
                                                .getCountryCode()
                                );

        if (
                domesticRoute
                        &&
                        distanceKm <= 1500.0
        ) {

            return 6;
        }

        if (
                domesticRoute
                        &&
                        distanceKm <= 3000.0
        ) {

            return 4;
        }

        if (
                distanceKm <= 2500.0
        ) {

            return 3;
        }

        if (
                distanceKm <= 6000.0
        ) {

            return 2;
        }

        return 1;
    }

    private List<LocalTime> buildDepartureTimes(
            int scheduleFrequency
    ) {

        List<LocalTime> departureTimePool =
                List.of(
                        LocalTime.of(
                                6,
                                0
                        ),
                        LocalTime.of(
                                8,
                                30
                        ),
                        LocalTime.of(
                                11,
                                0
                        ),
                        LocalTime.of(
                                13,
                                30
                        ),
                        LocalTime.of(
                                16,
                                0
                        ),
                        LocalTime.of(
                                18,
                                30
                        ),
                        LocalTime.of(
                                21,
                                0
                        ),
                        LocalTime.of(
                                23,
                                0
                        )
                );

        int frequency =
                Math.min(
                        scheduleFrequency,
                        departureTimePool.size()
                );

        return departureTimePool
                .subList(
                        0,
                        frequency
                );
    }

    private int resolveDurationMinutes(
            FlightRoute flightRoute
    ) {

        if (
                flightRoute
                        .getEstimatedDurationMinutes()
                        != null
                        &&
                        flightRoute
                                .getEstimatedDurationMinutes()
                                > 0
        ) {

            return flightRoute
                    .getEstimatedDurationMinutes();
        }

        double distanceKm =
                flightRoute.getDistanceKm() != null
                        ? flightRoute.getDistanceKm()
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

    private Aircraft findAvailableAircraft(
            List<Aircraft> activeAircraft,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime
    ) {

        for (
                Aircraft aircraft
                : activeAircraft
        ) {

            boolean aircraftAlreadyAssigned =
                    flightScheduleRepository
                            .existsByAircraftAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
                                    aircraft,
                                    arrivalTime,
                                    departureTime
                            );

            if (
                    !aircraftAlreadyAssigned
            ) {

                return aircraft;
            }
        }

        return null;
    }

    private void createFlightFares(
            FlightSchedule flightSchedule,
            Aircraft aircraft,
            FlightRoute flightRoute
    ) {

        List<FlightFare> flightFares =
                new ArrayList<>();

        BigDecimal economyFare =
                calculateEconomyFare(
                        flightRoute
                );

        if (
                aircraft.getEconomySeats() != null
                        &&
                        aircraft.getEconomySeats() > 0
        ) {

            flightFares.add(
                    buildFlightFare(
                            flightSchedule,
                            FareClass.ECONOMY,
                            economyFare,
                            aircraft.getEconomySeats()
                    )
            );
        }

        if (
                aircraft.getBusinessSeats() != null
                        &&
                        aircraft.getBusinessSeats() > 0
        ) {

            BigDecimal businessFare =
                    economyFare
                            .multiply(
                                    BigDecimal.valueOf(
                                            2.35
                                    )
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            flightFares.add(
                    buildFlightFare(
                            flightSchedule,
                            FareClass.BUSINESS,
                            businessFare,
                            aircraft.getBusinessSeats()
                    )
            );
        }

        if (
                aircraft.getFirstClassSeats() != null
                        &&
                        aircraft.getFirstClassSeats() > 0
        ) {

            BigDecimal firstClassFare =
                    economyFare
                            .multiply(
                                    BigDecimal.valueOf(
                                            4.50
                                    )
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            flightFares.add(
                    buildFlightFare(
                            flightSchedule,
                            FareClass.FIRST_CLASS,
                            firstClassFare,
                            aircraft.getFirstClassSeats()
                    )
            );
        }

        if (
                flightFares.isEmpty()
        ) {

            throw new RuntimeException(
                    "Selected aircraft has no passenger seat capacity."
            );
        }

        flightFareRepository.saveAll(
                flightFares
        );
    }

    private FlightFare buildFlightFare(
            FlightSchedule flightSchedule,
            FareClass fareClass,
            BigDecimal baseFare,
            Integer availableSeats
    ) {

        return FlightFare.builder()
                .flightSchedule(
                        flightSchedule
                )
                .fareClass(
                        fareClass
                )
                .baseFare(
                        baseFare
                )
                .availableSeats(
                        availableSeats
                )
                .active(true)
                .build();
    }

    private BigDecimal calculateEconomyFare(
            FlightRoute flightRoute
    ) {

        double distanceKm =
                flightRoute.getDistanceKm() != null
                        ? flightRoute.getDistanceKm()
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

    private String generateFlightNumber() {

        String flightNumber;

        do {

            String randomNumber =
                    UUID.randomUUID()
                            .toString()
                            .replace(
                                    "-",
                                    ""
                            )
                            .substring(
                                    0,
                                    5
                            )
                            .toUpperCase();

            flightNumber =
                    "SL"
                            + randomNumber;

        } while (
                flightScheduleRepository
                        .existsByFlightNumberIgnoreCase(
                                flightNumber
                        )
        );

        return flightNumber;
    }

    private FlightSearchResponse
    mapToFlightSearchResponse(
            FlightSchedule flightSchedule
    ) {

        FlightRoute flightRoute =
                flightSchedule.getFlightRoute();

        Airport sourceAirport =
                flightRoute.getSourceAirport();

        Airport destinationAirport =
                flightRoute.getDestinationAirport();

        Aircraft aircraft =
                flightSchedule.getAircraft();

        List<FlightFareResponse> fares =
                flightFareRepository
                        .findByFlightScheduleIdAndActiveTrue(
                                flightSchedule.getId()
                        )
                        .stream()
                        .filter(
                                flightFare ->
                                        flightFare
                                                .getAvailableSeats()
                                                > 0
                        )
                        .map(
                                this::mapToFlightFareResponse
                        )
                        .toList();

        return FlightSearchResponse.builder()
                .flightScheduleId(
                        flightSchedule.getId()
                )
                .flightNumber(
                        flightSchedule.getFlightNumber()
                )
                .flightRouteId(
                        flightRoute.getId()
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
                        flightRoute
                                .getEstimatedDurationMinutes()
                )
                .aircraftId(
                        aircraft.getId()
                )
                .aircraftRegistrationNumber(
                        aircraft.getRegistrationNumber()
                )
                .aircraftManufacturer(
                        aircraft.getManufacturer()
                )
                .aircraftModel(
                        aircraft.getModel()
                )
                .departureTime(
                        flightSchedule.getDepartureTime()
                )
                .arrivalTime(
                        flightSchedule.getArrivalTime()
                )
                .status(
                        flightSchedule.getStatus()
                )
                .fares(
                        fares
                )
                .build();
    }

    private FlightFareResponse
    mapToFlightFareResponse(
            FlightFare flightFare
    ) {

        FlightSchedule flightSchedule =
                flightFare.getFlightSchedule();

        FlightRoute flightRoute =
                flightSchedule.getFlightRoute();

        Airport sourceAirport =
                flightRoute.getSourceAirport();

        Airport destinationAirport =
                flightRoute.getDestinationAirport();

        return FlightFareResponse.builder()
                .id(
                        flightFare.getId()
                )
                .flightScheduleId(
                        flightSchedule.getId()
                )
                .flightNumber(
                        flightSchedule.getFlightNumber()
                )
                .sourceIataCode(
                        sourceAirport.getIataCode()
                )
                .destinationIataCode(
                        destinationAirport.getIataCode()
                )
                .fareClass(
                        flightFare.getFareClass()
                )
                .baseFare(
                        flightFare.getBaseFare()
                )
                .availableSeats(
                        flightFare.getAvailableSeats()
                )
                .active(
                        flightFare.isActive()
                )
                .createdAt(
                        flightFare.getCreatedAt()
                )
                .updatedAt(
                        flightFare.getUpdatedAt()
                )
                .build();
    }
}