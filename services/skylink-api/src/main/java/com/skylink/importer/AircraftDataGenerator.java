package com.skylink.importer;

import com.skylink.entity.Aircraft;
import com.skylink.repository.AircraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class AircraftDataGenerator
        implements CommandLineRunner {

    private static final int TARGET_FLEET_SIZE =
            300;

    private static final int BATCH_SIZE =
            100;

    private final AircraftRepository
            aircraftRepository;

    @Override
    @Transactional
    public void run(String... args) {

        log.info(
                "Starting SkyLink aircraft fleet generation..."
        );

        long existingSkyLinkAircraft =
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
                        .count();

        log.info(
                "Existing active SkyLink aircraft: {}",
                existingSkyLinkAircraft
        );

        if (
                existingSkyLinkAircraft
                        >= TARGET_FLEET_SIZE
        ) {

            log.info(
                    "SkyLink fleet already contains at least {} aircraft. Generation skipped.",
                    TARGET_FLEET_SIZE
            );

            return;
        }

        List<Aircraft> aircraftBatch =
                new ArrayList<>(
                        BATCH_SIZE
                );

        int generatedAircraft = 0;

        for (
                int aircraftNumber = 1;
                aircraftNumber
                        <= TARGET_FLEET_SIZE;
                aircraftNumber++
        ) {

            String registrationNumber =
                    generateRegistrationNumber(
                            aircraftNumber
                    );

            if (
                    aircraftRepository
                            .existsByRegistrationNumberIgnoreCase(
                                    registrationNumber
                            )
            ) {

                continue;
            }

            Aircraft aircraft =
                    buildAircraft(
                            aircraftNumber,
                            registrationNumber
                    );

            aircraftBatch.add(
                    aircraft
            );

            if (
                    aircraftBatch.size()
                            >= BATCH_SIZE
            ) {

                aircraftRepository.saveAll(
                        aircraftBatch
                );

                generatedAircraft +=
                        aircraftBatch.size();

                log.info(
                        "Generated {} SkyLink aircraft...",
                        generatedAircraft
                );

                aircraftBatch.clear();
            }
        }

        if (
                !aircraftBatch.isEmpty()
        ) {

            aircraftRepository.saveAll(
                    aircraftBatch
            );

            generatedAircraft +=
                    aircraftBatch.size();

            aircraftBatch.clear();
        }

        log.info(
                "========================================"
        );

        log.info(
                "SkyLink aircraft fleet generation completed."
        );

        log.info(
                "New aircraft generated: {}",
                generatedAircraft
        );

        log.info(
                "Total aircraft currently in database: {}",
                aircraftRepository.count()
        );

        log.info(
                "Target SkyLink fleet size: {}",
                TARGET_FLEET_SIZE
        );

        log.info(
                "========================================"
        );
    }

    private Aircraft buildAircraft(
            int aircraftNumber,
            String registrationNumber
    ) {

        int fleetType =
                aircraftNumber % 8;

        return switch (fleetType) {

            case 0 ->
                    createAircraft(
                            registrationNumber,
                            "Airbus",
                            "A350-900",
                            315,
                            245,
                            52,
                            18
                    );

            case 1 ->
                    createAircraft(
                            registrationNumber,
                            "Airbus",
                            "A320-200",
                            180,
                            150,
                            30,
                            0
                    );

            case 2 ->
                    createAircraft(
                            registrationNumber,
                            "Airbus",
                            "A321neo",
                            220,
                            184,
                            36,
                            0
                    );

            case 3 ->
                    createAircraft(
                            registrationNumber,
                            "Boeing",
                            "737 MAX 8",
                            178,
                            150,
                            28,
                            0
                    );

            case 4 ->
                    createAircraft(
                            registrationNumber,
                            "Boeing",
                            "787-9 Dreamliner",
                            296,
                            216,
                            48,
                            32
                    );

            case 5 ->
                    createAircraft(
                            registrationNumber,
                            "Boeing",
                            "777-300ER",
                            354,
                            264,
                            56,
                            34
                    );

            case 6 ->
                    createAircraft(
                            registrationNumber,
                            "Airbus",
                            "A330-900neo",
                            287,
                            215,
                            48,
                            24
                    );

            default ->
                    createAircraft(
                            registrationNumber,
                            "Airbus",
                            "A321XLR",
                            206,
                            166,
                            32,
                            8
                    );
        };
    }

    private Aircraft createAircraft(
            String registrationNumber,
            String manufacturer,
            String model,
            int totalSeats,
            int economySeats,
            int businessSeats,
            int firstClassSeats
    ) {

        validateSeatCapacity(
                registrationNumber,
                totalSeats,
                economySeats,
                businessSeats,
                firstClassSeats
        );

        return Aircraft.builder()
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
                        economySeats
                )
                .businessSeats(
                        businessSeats
                )
                .firstClassSeats(
                        firstClassSeats
                )
                .active(true)
                .build();
    }

    private void validateSeatCapacity(
            String registrationNumber,
            int totalSeats,
            int economySeats,
            int businessSeats,
            int firstClassSeats
    ) {

        int configuredSeats =
                economySeats
                        + businessSeats
                        + firstClassSeats;

        if (
                configuredSeats
                        != totalSeats
        ) {

            throw new IllegalStateException(
                    "Invalid seat capacity for aircraft "
                            + registrationNumber
                            + ". Total seats: "
                            + totalSeats
                            + ", configured seats: "
                            + configuredSeats
            );
        }
    }

    private String generateRegistrationNumber(
            int aircraftNumber
    ) {

        return String.format(
                "VT-SLA%04d",
                aircraftNumber
        );
    }
}