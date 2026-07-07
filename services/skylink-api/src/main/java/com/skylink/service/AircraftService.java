package com.skylink.service;

import com.skylink.dto.AircraftResponse;
import com.skylink.dto.CreateAircraftRequest;
import com.skylink.entity.Aircraft;
import com.skylink.repository.AircraftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AircraftService {

    private final AircraftRepository aircraftRepository;

    public AircraftService(
            AircraftRepository aircraftRepository
    ) {
        this.aircraftRepository =
                aircraftRepository;
    }

    @Transactional
    public AircraftResponse createAircraft(
            CreateAircraftRequest request
    ) {

        String registrationNumber =
                request.getRegistrationNumber()
                        .trim()
                        .toUpperCase();

        String manufacturer =
                request.getManufacturer()
                        .trim();

        String model =
                request.getModel()
                        .trim();

        if (aircraftRepository
                .existsByRegistrationNumberIgnoreCase(
                        registrationNumber
                )) {

            throw new RuntimeException(
                    "Aircraft already exists with registration number: "
                            + registrationNumber
            );
        }

        int totalSeats =
                request.getEconomySeats()
                        + request.getBusinessSeats()
                        + request.getFirstClassSeats();

        if (totalSeats <= 0) {

            throw new RuntimeException(
                    "Aircraft must have at least one seat."
            );
        }

        Aircraft aircraft =
                Aircraft.builder()
                        .registrationNumber(
                                registrationNumber
                        )
                        .manufacturer(
                                manufacturer
                        )
                        .model(
                                model
                        )
                        .totalSeats(
                                totalSeats
                        )
                        .economySeats(
                                request.getEconomySeats()
                        )
                        .businessSeats(
                                request.getBusinessSeats()
                        )
                        .firstClassSeats(
                                request.getFirstClassSeats()
                        )
                        .active(true)
                        .build();

        Aircraft savedAircraft =
                aircraftRepository.save(
                        aircraft
                );

        return mapToAircraftResponse(
                savedAircraft
        );
    }

    @Transactional(readOnly = true)
    public List<AircraftResponse> getAllAircraft() {

        return aircraftRepository
                .findAll()
                .stream()
                .map(this::mapToAircraftResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AircraftResponse> getActiveAircraft() {

        return aircraftRepository
                .findByActiveTrue()
                .stream()
                .map(this::mapToAircraftResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AircraftResponse getAircraftById(
            Long aircraftId
    ) {

        if (aircraftId == null) {

            throw new RuntimeException(
                    "Aircraft ID is required."
            );
        }

        Aircraft aircraft =
                aircraftRepository
                        .findById(aircraftId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Aircraft not found with ID: "
                                                + aircraftId
                                )
                        );

        return mapToAircraftResponse(
                aircraft
        );
    }

    @Transactional(readOnly = true)
    public AircraftResponse getAircraftByRegistrationNumber(
            String registrationNumber
    ) {

        if (registrationNumber == null
                || registrationNumber.isBlank()) {

            throw new RuntimeException(
                    "Aircraft registration number is required."
            );
        }

        String normalizedRegistrationNumber =
                registrationNumber
                        .trim()
                        .toUpperCase();

        Aircraft aircraft =
                aircraftRepository
                        .findByRegistrationNumberIgnoreCase(
                                normalizedRegistrationNumber
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Aircraft not found with registration number: "
                                                + normalizedRegistrationNumber
                                )
                        );

        return mapToAircraftResponse(
                aircraft
        );
    }

    private AircraftResponse mapToAircraftResponse(
            Aircraft aircraft
    ) {

        return AircraftResponse.builder()
                .id(
                        aircraft.getId()
                )
                .registrationNumber(
                        aircraft.getRegistrationNumber()
                )
                .manufacturer(
                        aircraft.getManufacturer()
                )
                .model(
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
                .active(
                        aircraft.isActive()
                )
                .createdAt(
                        aircraft.getCreatedAt()
                )
                .updatedAt(
                        aircraft.getUpdatedAt()
                )
                .build();
    }
}