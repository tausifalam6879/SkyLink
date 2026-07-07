package com.skylink.importer;

import com.skylink.entity.Aircraft;
import com.skylink.repository.AircraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class AircraftFleetSeeder implements CommandLineRunner {

    private final AircraftRepository aircraftRepository;

    @Override
    public void run(String... args) {

        log.info(
                "Starting SkyLink aircraft fleet seed..."
        );

        List<Aircraft> aircraftFleet =
                List.of(

                        createAircraft(
                                "VT-SLA001",
                                "Airbus",
                                "A320-200",
                                180,
                                150,
                                30,
                                0
                        ),

                        createAircraft(
                                "VT-SLA002",
                                "Airbus",
                                "A320neo",
                                186,
                                156,
                                30,
                                0
                        ),

                        createAircraft(
                                "VT-SLA003",
                                "Airbus",
                                "A321neo",
                                220,
                                184,
                                36,
                                0
                        ),

                        createAircraft(
                                "VT-SLA004",
                                "Boeing",
                                "737-800",
                                189,
                                159,
                                30,
                                0
                        ),

                        createAircraft(
                                "VT-SLA005",
                                "Boeing",
                                "737 MAX 8",
                                197,
                                165,
                                32,
                                0
                        ),

                        createAircraft(
                                "VT-SLA006",
                                "Airbus",
                                "A330-300",
                                300,
                                240,
                                48,
                                12
                        ),

                        createAircraft(
                                "VT-SLA007",
                                "Airbus",
                                "A350-900",
                                325,
                                255,
                                54,
                                16
                        ),

                        createAircraft(
                                "VT-SLA008",
                                "Boeing",
                                "787-8 Dreamliner",
                                248,
                                194,
                                42,
                                12
                        ),

                        createAircraft(
                                "VT-SLA009",
                                "Boeing",
                                "787-9 Dreamliner",
                                290,
                                224,
                                50,
                                16
                        ),

                        createAircraft(
                                "VT-SLA010",
                                "Boeing",
                                "777-300ER",
                                350,
                                270,
                                60,
                                20
                        ),

                        createAircraft(
                                "VT-SLA011",
                                "ATR",
                                "72-600",
                                78,
                                78,
                                0,
                                0
                        ),

                        createAircraft(
                                "VT-SLA012",
                                "Embraer",
                                "E195-E2",
                                132,
                                112,
                                20,
                                0
                        )
                );

        int importedAircraft = 0;
        int skippedAircraft = 0;

        for (Aircraft aircraft : aircraftFleet) {

            boolean alreadyExists =
                    aircraftRepository
                            .existsByRegistrationNumberIgnoreCase(
                                    aircraft.getRegistrationNumber()
                            );

            if (alreadyExists) {

                skippedAircraft++;

                log.info(
                        "Aircraft already exists. Skipping: {}",
                        aircraft.getRegistrationNumber()
                );

                continue;
            }

            aircraftRepository.save(
                    aircraft
            );

            importedAircraft++;

            log.info(
                    "Aircraft seeded: {} - {} {}",
                    aircraft.getRegistrationNumber(),
                    aircraft.getManufacturer(),
                    aircraft.getModel()
            );
        }

        log.info(
                "========================================"
        );

        log.info(
                "SkyLink aircraft fleet seed completed."
        );

        log.info(
                "New aircraft seeded: {}",
                importedAircraft
        );

        log.info(
                "Existing aircraft skipped: {}",
                skippedAircraft
        );

        log.info(
                "Total aircraft currently in database: {}",
                aircraftRepository.count()
        );

        log.info(
                "========================================"
        );
    }

    private Aircraft createAircraft(
            String registrationNumber,
            String manufacturer,
            String model,
            Integer totalSeats,
            Integer economySeats,
            Integer businessSeats,
            Integer firstClassSeats
    ) {

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
}