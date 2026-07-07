package com.skylink.service;

import com.skylink.dto.CreateFlightFareRequest;
import com.skylink.dto.FlightFareResponse;
import com.skylink.entity.Aircraft;
import com.skylink.entity.Airport;
import com.skylink.entity.FareClass;
import com.skylink.entity.FlightFare;
import com.skylink.entity.FlightRoute;
import com.skylink.entity.FlightSchedule;
import com.skylink.repository.FlightFareRepository;
import com.skylink.repository.FlightScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlightFareService {

    private final FlightFareRepository flightFareRepository;

    private final FlightScheduleRepository flightScheduleRepository;

    public FlightFareService(
            FlightFareRepository flightFareRepository,
            FlightScheduleRepository flightScheduleRepository
    ) {
        this.flightFareRepository =
                flightFareRepository;

        this.flightScheduleRepository =
                flightScheduleRepository;
    }

    @Transactional
    public FlightFareResponse createFlightFare(
            CreateFlightFareRequest request
    ) {

        FlightSchedule flightSchedule =
                flightScheduleRepository
                        .findById(
                                request.getFlightScheduleId()
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Flight schedule not found with ID: "
                                                + request.getFlightScheduleId()
                                )
                        );

        if (!flightSchedule.isActive()) {

            throw new RuntimeException(
                    "Flight schedule is inactive."
            );
        }

        FareClass fareClass =
                request.getFareClass();

        if (flightFareRepository
                .existsByFlightScheduleAndFareClass(
                        flightSchedule,
                        fareClass
                )) {

            throw new RuntimeException(
                    "Fare already exists for flight "
                            + flightSchedule.getFlightNumber()
                            + " and class "
                            + fareClass
            );
        }

        Aircraft aircraft =
                flightSchedule.getAircraft();

        int classSeatCapacity =
                getClassSeatCapacity(
                        aircraft,
                        fareClass
                );

        if (classSeatCapacity <= 0) {

            throw new RuntimeException(
                    "Aircraft "
                            + aircraft.getRegistrationNumber()
                            + " does not support fare class "
                            + fareClass
            );
        }

        if (request.getAvailableSeats()
                > classSeatCapacity) {

            throw new RuntimeException(
                    "Available seats cannot exceed "
                            + fareClass
                            + " seat capacity of "
                            + classSeatCapacity
            );
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
                                request.getBaseFare()
                        )
                        .availableSeats(
                                request.getAvailableSeats()
                        )
                        .active(true)
                        .build();

        FlightFare savedFlightFare =
                flightFareRepository.save(
                        flightFare
                );

        return mapToFlightFareResponse(
                savedFlightFare
        );
    }

    @Transactional(readOnly = true)
    public List<FlightFareResponse> getAllFlightFares() {

        return flightFareRepository
                .findAll()
                .stream()
                .map(this::mapToFlightFareResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FlightFareResponse> getActiveFlightFares() {

        return flightFareRepository
                .findByActiveTrue()
                .stream()
                .map(this::mapToFlightFareResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FlightFareResponse getFlightFareById(
            Long flightFareId
    ) {

        if (flightFareId == null) {

            throw new RuntimeException(
                    "Flight fare ID is required."
            );
        }

        FlightFare flightFare =
                flightFareRepository
                        .findById(
                                flightFareId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Flight fare not found with ID: "
                                                + flightFareId
                                )
                        );

        return mapToFlightFareResponse(
                flightFare
        );
    }

    @Transactional(readOnly = true)
    public List<FlightFareResponse> getFaresByFlightSchedule(
            Long flightScheduleId
    ) {

        if (flightScheduleId == null) {

            throw new RuntimeException(
                    "Flight schedule ID is required."
            );
        }

        if (!flightScheduleRepository
                .existsById(
                        flightScheduleId
                )) {

            throw new RuntimeException(
                    "Flight schedule not found with ID: "
                            + flightScheduleId
            );
        }

        return flightFareRepository
                .findByFlightScheduleIdAndActiveTrue(
                        flightScheduleId
                )
                .stream()
                .map(this::mapToFlightFareResponse)
                .toList();
    }

    private int getClassSeatCapacity(
            Aircraft aircraft,
            FareClass fareClass
    ) {

        return switch (fareClass) {

            case ECONOMY ->
                    aircraft.getEconomySeats();

            case BUSINESS ->
                    aircraft.getBusinessSeats();

            case FIRST_CLASS ->
                    aircraft.getFirstClassSeats();
        };
    }

    private FlightFareResponse mapToFlightFareResponse(
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