package com.skylink.importer;

import com.skylink.entity.Aircraft;
import com.skylink.entity.FareClass;
import com.skylink.entity.FlightSchedule;
import com.skylink.entity.Seat;
import com.skylink.repository.FlightScheduleRepository;
import com.skylink.repository.SeatRepository;
import jakarta.persistence.EntityManager;
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
@Order(5)
public class SeatDataGenerator implements CommandLineRunner {

    private final FlightScheduleRepository
            flightScheduleRepository;

    private final SeatRepository
            seatRepository;

    private final EntityManager
            entityManager;

    @Override
    @Transactional
    public void run(String... args) {

        log.info(
                "Starting SkyLink optimized seat map generation..."
        );

        @SuppressWarnings("unchecked")
        List<Number> scheduleIds =
                entityManager
                        .createNativeQuery("""
                                SELECT fs.id
                                FROM flight_schedules fs
                                WHERE fs.active = true
                                  AND fs.aircraft_id IS NOT NULL
                                  AND NOT EXISTS (
                                      SELECT 1
                                      FROM seats s
                                      WHERE s.flight_schedule_id = fs.id
                                        AND s.active = true
                                  )
                                ORDER BY fs.id
                                """)
                        .getResultList();

        log.info(
                "Flight schedules requiring seat maps: {}",
                scheduleIds.size()
        );

        int generatedSeatMaps = 0;
        int generatedSeats = 0;
        int skippedSchedules = 0;

        for (Number scheduleId : scheduleIds) {

            Long id =
                    scheduleId.longValue();

            FlightSchedule flightSchedule =
                    flightScheduleRepository
                            .findById(id)
                            .orElse(null);

            if (flightSchedule == null) {

                skippedSchedules++;

                log.warn(
                        "Flight schedule {} not found. Skipping.",
                        id
                );

                continue;
            }

            int createdSeats =
                    generateSeatMapForSchedule(
                            flightSchedule
                    );

            if (createdSeats > 0) {

                generatedSeatMaps++;

                generatedSeats +=
                        createdSeats;

                log.info(
                        "Seat map generated for flight {} | Schedule ID {} | Seats {}",
                        flightSchedule.getFlightNumber(),
                        flightSchedule.getId(),
                        createdSeats
                );

            } else {

                skippedSchedules++;
            }
        }

        entityManager.flush();

        log.info(
                "========================================"
        );

        log.info(
                "SkyLink seat map generation completed."
        );

        log.info(
                "Generated seat maps: {}",
                generatedSeatMaps
        );

        log.info(
                "Generated seats: {}",
                generatedSeats
        );

        log.info(
                "Skipped schedules: {}",
                skippedSchedules
        );

        log.info(
                "Total seats currently in database: {}",
                seatRepository.count()
        );

        log.info(
                "========================================"
        );
    }

    @Transactional
    public int generateSeatMapForSchedule(
            FlightSchedule flightSchedule
    ) {

        if (flightSchedule == null) {

            log.warn(
                    "Flight schedule is null. Seat map generation skipped."
            );

            return 0;
        }

        if (!flightSchedule.isActive()) {

            log.warn(
                    "Flight schedule {} is inactive. Seat map generation skipped.",
                    flightSchedule.getId()
            );

            return 0;
        }

        if (flightSchedule.getAircraft() == null) {

            log.warn(
                    "Flight schedule {} has no aircraft. Seat map generation skipped.",
                    flightSchedule.getId()
            );

            return 0;
        }

        boolean seatMapAlreadyExists =
                entityManager
                        .createQuery(
                                """
                                SELECT COUNT(s)
                                FROM Seat s
                                WHERE s.flightSchedule = :flightSchedule
                                  AND s.active = true
                                """,
                                Long.class
                        )
                        .setParameter(
                                "flightSchedule",
                                flightSchedule
                        )
                        .getSingleResult()
                        > 0;

        if (seatMapAlreadyExists) {

            log.debug(
                    "Seat map already exists for schedule {}. Skipping.",
                    flightSchedule.getId()
            );

            return 0;
        }

        return generateSeatMap(
                flightSchedule
        );
    }

    private int generateSeatMap(
            FlightSchedule flightSchedule
    ) {

        Aircraft aircraft =
                flightSchedule.getAircraft();

        int businessSeats =
                safeSeatCount(
                        aircraft.getBusinessSeats()
                );

        int firstClassSeats =
                safeSeatCount(
                        aircraft.getFirstClassSeats()
                );

        int economySeats =
                safeSeatCount(
                        aircraft.getEconomySeats()
                );

        List<Seat> seats =
                new ArrayList<>();

        int currentRow = 1;

        if (firstClassSeats > 0) {

            SeatGenerationResult result =
                    generateCabinSeats(
                            seats,
                            flightSchedule,
                            FareClass.FIRST_CLASS,
                            firstClassSeats,
                            currentRow,
                            new String[]{
                                    "A",
                                    "C",
                                    "D",
                                    "F"
                            },
                            true
                    );

            currentRow =
                    result.nextRow();
        }

        if (businessSeats > 0) {

            SeatGenerationResult result =
                    generateCabinSeats(
                            seats,
                            flightSchedule,
                            FareClass.BUSINESS,
                            businessSeats,
                            currentRow,
                            new String[]{
                                    "A",
                                    "C",
                                    "D",
                                    "F"
                            },
                            true
                    );

            currentRow =
                    result.nextRow();
        }

        if (economySeats > 0) {

            generateCabinSeats(
                    seats,
                    flightSchedule,
                    FareClass.ECONOMY,
                    economySeats,
                    currentRow,
                    new String[]{
                            "A",
                            "B",
                            "C",
                            "D",
                            "E",
                            "F"
                    },
                    false
            );
        }

        if (seats.isEmpty()) {

            log.warn(
                    "Aircraft {} has zero configured seats. Schedule {} skipped.",
                    aircraft.getRegistrationNumber(),
                    flightSchedule.getId()
            );

            return 0;
        }

        seatRepository.saveAll(
                seats
        );

        seatRepository.flush();

        log.info(
                "Generated {} seats for flight {} | Schedule ID {} | Aircraft {}",
                seats.size(),
                flightSchedule.getFlightNumber(),
                flightSchedule.getId(),
                aircraft.getRegistrationNumber()
        );

        return seats.size();
    }

    private SeatGenerationResult generateCabinSeats(
            List<Seat> seats,
            FlightSchedule flightSchedule,
            FareClass fareClass,
            int seatCount,
            int startRow,
            String[] seatLetters,
            boolean premiumCabin
    ) {

        int generatedSeats = 0;

        int currentRow =
                startRow;

        while (
                generatedSeats < seatCount
        ) {

            for (
                    String seatLetter
                    : seatLetters
            ) {

                if (
                        generatedSeats
                                >= seatCount
                ) {

                    break;
                }

                String seatNumber =
                        currentRow
                                + seatLetter;

                boolean windowSeat =
                        seatLetter.equals("A")
                                ||
                                seatLetter.equals("F");

                boolean aisleSeat =
                        seatLetter.equals("C")
                                ||
                                seatLetter.equals("D");

                boolean extraLegroom =
                        premiumCabin
                                ||
                                isEconomyExtraLegroomRow(
                                        fareClass,
                                        currentRow,
                                        startRow
                                );

                Seat seat =
                        Seat.builder()
                                .flightSchedule(
                                        flightSchedule
                                )
                                .seatNumber(
                                        seatNumber
                                )
                                .fareClass(
                                        fareClass
                                )
                                .rowNumber(
                                        currentRow
                                )
                                .seatLetter(
                                        seatLetter
                                )
                                .windowSeat(
                                        windowSeat
                                )
                                .aisleSeat(
                                        aisleSeat
                                )
                                .extraLegroom(
                                        extraLegroom
                                )
                                .booked(false)
                                .active(true)
                                .build();

                seats.add(
                        seat
                );

                generatedSeats++;
            }

            currentRow++;
        }

        return new SeatGenerationResult(
                generatedSeats,
                currentRow
        );
    }

    private boolean isEconomyExtraLegroomRow(
            FareClass fareClass,
            int currentRow,
            int cabinStartRow
    ) {

        if (
                fareClass
                        != FareClass.ECONOMY
        ) {

            return false;
        }

        return currentRow
                == cabinStartRow;
    }

    private int safeSeatCount(
            Integer seatCount
    ) {

        return seatCount != null
                ? Math.max(
                seatCount,
                0
        )
                : 0;
    }

    private record SeatGenerationResult(
            int generatedSeats,
            int nextRow
    ) {
    }
}