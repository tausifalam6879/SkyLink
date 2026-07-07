package com.skylink.service;

import com.skylink.dto.CreateFlightScheduleRequest;
import com.skylink.dto.FlightScheduleResponse;
import com.skylink.entity.Aircraft;
import com.skylink.entity.Airport;
import com.skylink.entity.FlightRoute;
import com.skylink.entity.FlightSchedule;
import com.skylink.entity.FlightStatus;
import com.skylink.repository.AircraftRepository;
import com.skylink.repository.FlightRouteRepository;
import com.skylink.repository.FlightScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class FlightScheduleService {

    private final FlightScheduleRepository flightScheduleRepository;

    private final FlightRouteRepository flightRouteRepository;

    private final AircraftRepository aircraftRepository;

    private final FlightRouteService flightRouteService;

    public FlightScheduleService(
            FlightScheduleRepository flightScheduleRepository,
            FlightRouteRepository flightRouteRepository,
            AircraftRepository aircraftRepository,
            FlightRouteService flightRouteService
    ) {
        this.flightScheduleRepository =
                flightScheduleRepository;

        this.flightRouteRepository =
                flightRouteRepository;

        this.aircraftRepository =
                aircraftRepository;

        this.flightRouteService =
                flightRouteService;
    }

    @Transactional
    public FlightScheduleResponse createFlightSchedule(
            CreateFlightScheduleRequest request
    ) {

        if (request == null) {

            throw new RuntimeException(
                    "Flight schedule request is required."
            );
        }

        if (request.getFlightNumber() == null
                || request.getFlightNumber().isBlank()) {

            throw new RuntimeException(
                    "Flight number is required."
            );
        }

        if (request.getSourceIataCode() == null
                || request.getSourceIataCode().isBlank()) {

            throw new RuntimeException(
                    "Source IATA code is required."
            );
        }

        if (request.getDestinationIataCode() == null
                || request.getDestinationIataCode().isBlank()) {

            throw new RuntimeException(
                    "Destination IATA code is required."
            );
        }

        if (request.getAircraftId() == null) {

            throw new RuntimeException(
                    "Aircraft ID is required."
            );
        }

        if (request.getDepartureTime() == null) {

            throw new RuntimeException(
                    "Departure time is required."
            );
        }

        String flightNumber =
                request.getFlightNumber()
                        .trim()
                        .toUpperCase();

        LocalDateTime departureTime =
                request.getDepartureTime();

        if (!departureTime.isAfter(
                LocalDateTime.now()
        )) {

            throw new RuntimeException(
                    "Departure time must be in the future."
            );
        }

        if (flightScheduleRepository
                .existsByFlightNumberIgnoreCaseAndDepartureTime(
                        flightNumber,
                        departureTime
                )) {

            throw new RuntimeException(
                    "Flight schedule already exists for flight number "
                            + flightNumber
                            + " at this departure time."
            );
        }

        FlightRoute flightRoute =
                flightRouteRepository
                        .findBySourceAirportIataCodeIgnoreCaseAndDestinationAirportIataCodeIgnoreCase(
                                request.getSourceIataCode()
                                        .trim()
                                        .toUpperCase(),
                                request.getDestinationIataCode()
                                        .trim()
                                        .toUpperCase()
                        )
                        .orElseGet(
                                () -> {

                                    Long routeId =
                                            flightRouteService
                                                    .resolveOrCreateRoute(
                                                            request.getSourceIataCode(),
                                                            request.getDestinationIataCode()
                                                    )
                                                    .getId();

                                    return flightRouteRepository
                                            .findById(
                                                    routeId
                                            )
                                            .orElseThrow(
                                                    () -> new RuntimeException(
                                                            "Flight route not found with ID: "
                                                                    + routeId
                                                    )
                                            );
                                }
                        );

        Aircraft aircraft =
                aircraftRepository
                        .findById(
                                request.getAircraftId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Aircraft not found with ID: "
                                                + request.getAircraftId()
                                )
                        );

        if (!aircraft.isActive()) {

            throw new RuntimeException(
                    "Aircraft is inactive."
            );
        }

        LocalDateTime arrivalTime =
                departureTime.plusMinutes(
                        flightRoute.getEstimatedDurationMinutes()
                );

        boolean aircraftAlreadyAssigned =
                flightScheduleRepository
                        .existsByAircraftAndDepartureTimeLessThanAndArrivalTimeGreaterThan(
                                aircraft,
                                arrivalTime,
                                departureTime
                        );

        if (aircraftAlreadyAssigned) {

            throw new RuntimeException(
                    "Aircraft "
                            + aircraft.getRegistrationNumber()
                            + " is already assigned to another flight during this time."
            );
        }

        FlightSchedule flightSchedule =
                FlightSchedule.builder()
                        .flightNumber(
                                flightNumber
                        )
                        .flightRoute(
                                flightRoute
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

        FlightSchedule savedFlightSchedule =
                flightScheduleRepository.save(
                        flightSchedule
                );

        return mapToFlightScheduleResponse(
                savedFlightSchedule
        );
    }

    @Transactional(readOnly = true)
    public List<FlightScheduleResponse> getAllFlightSchedules() {

        return flightScheduleRepository
                .findAll()
                .stream()
                .map(this::mapToFlightScheduleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FlightScheduleResponse> getActiveFlightSchedules() {

        return flightScheduleRepository
                .findByActiveTrue()
                .stream()
                .map(this::mapToFlightScheduleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FlightScheduleResponse getFlightScheduleById(
            Long flightScheduleId
    ) {

        if (flightScheduleId == null) {

            throw new RuntimeException(
                    "Flight schedule ID is required."
            );
        }

        FlightSchedule flightSchedule =
                flightScheduleRepository
                        .findById(
                                flightScheduleId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Flight schedule not found with ID: "
                                                + flightScheduleId
                                )
                        );

        return mapToFlightScheduleResponse(
                flightSchedule
        );
    }

    @Transactional(readOnly = true)
    public List<FlightScheduleResponse> searchFlights(
            String sourceIataCode,
            String destinationIataCode,
            LocalDate travelDate
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

        if (travelDate == null) {

            throw new RuntimeException(
                    "Travel date is required."
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

        LocalDateTime startDateTime =
                travelDate.atStartOfDay();

        LocalDateTime endDateTime =
                travelDate.atTime(
                        LocalTime.MAX
                );

        return flightScheduleRepository
                .findByFlightRouteSourceAirportIataCodeIgnoreCaseAndFlightRouteDestinationAirportIataCodeIgnoreCaseAndDepartureTimeBetweenAndActiveTrueOrderByDepartureTimeAsc(
                        normalizedSourceCode,
                        normalizedDestinationCode,
                        startDateTime,
                        endDateTime
                )
                .stream()
                .map(this::mapToFlightScheduleResponse)
                .toList();
    }

    private FlightScheduleResponse mapToFlightScheduleResponse(
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

        return FlightScheduleResponse.builder()
                .id(
                        flightSchedule.getId()
                )
                .flightNumber(
                        flightSchedule.getFlightNumber()
                )
                .flightRouteId(
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
                .totalSeats(
                        aircraft.getTotalSeats()
                )
                .economySeats(
                        aircraft.getEconomySeats()
                )
                .businessSeats(
                        aircraft.getBusinessSeats()
                )
                .firstClassSeats(
                        aircraft.getFirstClassSeats()
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
                .active(
                        flightSchedule.isActive()
                )
                .createdAt(
                        flightSchedule.getCreatedAt()
                )
                .updatedAt(
                        flightSchedule.getUpdatedAt()
                )
                .build();
    }
}