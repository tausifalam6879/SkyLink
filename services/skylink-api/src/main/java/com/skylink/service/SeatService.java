package com.skylink.service;

import com.skylink.dto.SeatResponse;
import com.skylink.entity.FareClass;
import com.skylink.entity.FlightSchedule;
import com.skylink.entity.Seat;
import com.skylink.repository.FlightScheduleRepository;
import com.skylink.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    private final FlightScheduleRepository
            flightScheduleRepository;

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeats(
            Long flightScheduleId,
            FareClass fareClass
    ) {

        FlightSchedule flightSchedule =
                getFlightSchedule(
                        flightScheduleId
                );

        return seatRepository
                .findByFlightScheduleAndFareClassAndActiveTrueOrderByRowNumberAscSeatLetterAsc(
                        flightSchedule,
                        fareClass
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getAllSeats(
            Long flightScheduleId
    ) {

        FlightSchedule flightSchedule =
                getFlightSchedule(
                        flightScheduleId
                );

        return seatRepository
                .findByFlightScheduleAndActiveTrueOrderByRowNumberAscSeatLetterAsc(
                        flightSchedule
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getAvailableSeatCount(
            Long flightScheduleId,
            FareClass fareClass
    ) {

        FlightSchedule flightSchedule =
                getFlightSchedule(
                        flightScheduleId
                );

        return seatRepository
                .countByFlightScheduleAndFareClassAndBookedFalseAndActiveTrue(
                        flightSchedule,
                        fareClass
                );
    }

    @Transactional
    public Seat getAvailableSeat(
            FlightSchedule flightSchedule,
            FareClass fareClass,
            String seatNumber
    ) {

        if (
                seatNumber == null
                        ||
                        seatNumber.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Seat number is required."
            );
        }

        Seat seat =
                seatRepository
                        .findByFlightScheduleAndSeatNumberIgnoreCase(
                                flightSchedule,
                                seatNumber.trim()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Seat not found: "
                                                        + seatNumber
                                        )
                        );

        if (!seat.isActive()) {

            throw new IllegalStateException(
                    "Seat is not active."
            );
        }

        if (
                seat.getFareClass()
                        != fareClass
        ) {

            throw new IllegalArgumentException(
                    "Seat "
                            + seat.getSeatNumber()
                            + " does not belong to "
                            + fareClass
                            + " fare class."
            );
        }

        if (seat.isBooked()) {

            throw new IllegalStateException(
                    "Seat "
                            + seat.getSeatNumber()
                            + " is already booked."
            );
        }

        return seat;
    }

    @Transactional
    public void markSeatBooked(
            Seat seat
    ) {

        seat.setBooked(true);

        seatRepository.save(seat);
    }

    @Transactional
    public void releaseSeat(
            FlightSchedule flightSchedule,
            String seatNumber
    ) {

        if (
                seatNumber == null
                        ||
                        seatNumber.isBlank()
        ) {

            return;
        }

        seatRepository
                .findByFlightScheduleAndSeatNumberIgnoreCase(
                        flightSchedule,
                        seatNumber.trim()
                )
                .ifPresent(
                        seat -> {

                            seat.setBooked(false);

                            seatRepository.save(
                                    seat
                            );
                        }
                );
    }

    private FlightSchedule getFlightSchedule(
            Long flightScheduleId
    ) {

        return flightScheduleRepository
                .findById(flightScheduleId)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Flight schedule not found."
                                )
                );
    }

    private SeatResponse mapToResponse(
            Seat seat
    ) {

        return SeatResponse.builder()
                .id(
                        seat.getId()
                )
                .seatNumber(
                        seat.getSeatNumber()
                )
                .fareClass(
                        seat.getFareClass()
                )
                .rowNumber(
                        seat.getRowNumber()
                )
                .seatLetter(
                        seat.getSeatLetter()
                )
                .windowSeat(
                        seat.isWindowSeat()
                )
                .aisleSeat(
                        seat.isAisleSeat()
                )
                .extraLegroom(
                        seat.isExtraLegroom()
                )
                .booked(
                        seat.isBooked()
                )
                .available(
                        seat.isActive()
                                &&
                                !seat.isBooked()
                )
                .build();
    }
}