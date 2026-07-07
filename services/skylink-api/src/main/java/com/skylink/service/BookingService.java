package com.skylink.service;

import com.skylink.dto.BookingResponse;
import com.skylink.dto.CreateBookingRequest;
import com.skylink.dto.PassengerRequest;
import com.skylink.dto.PassengerResponse;
import com.skylink.entity.Airport;
import com.skylink.entity.Booking;
import com.skylink.entity.BookingStatus;
import com.skylink.entity.FlightFare;
import com.skylink.entity.FlightRoute;
import com.skylink.entity.FlightSchedule;
import com.skylink.entity.Passenger;
import com.skylink.entity.Seat;
import com.skylink.entity.User;
import com.skylink.repository.BookingRepository;
import com.skylink.repository.FlightFareRepository;
import com.skylink.repository.FlightScheduleRepository;
import com.skylink.repository.PassengerRepository;
import com.skylink.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    private final PassengerRepository passengerRepository;

    private final FlightScheduleRepository
            flightScheduleRepository;

    private final FlightFareRepository
            flightFareRepository;

    private final UserRepository userRepository;

    private final SeatService seatService;

    public BookingService(
            BookingRepository bookingRepository,
            PassengerRepository passengerRepository,
            FlightScheduleRepository flightScheduleRepository,
            FlightFareRepository flightFareRepository,
            UserRepository userRepository,
            SeatService seatService
    ) {

        this.bookingRepository =
                bookingRepository;

        this.passengerRepository =
                passengerRepository;

        this.flightScheduleRepository =
                flightScheduleRepository;

        this.flightFareRepository =
                flightFareRepository;

        this.userRepository =
                userRepository;

        this.seatService =
                seatService;
    }

    @Transactional
    public BookingResponse createBooking(
            CreateBookingRequest request
    ) {

        User user =
                getCurrentAuthenticatedUser();

        FlightSchedule flightSchedule =
                flightScheduleRepository
                        .findById(
                                request.getFlightScheduleId()
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Flight schedule not found with ID: "
                                                        + request.getFlightScheduleId()
                                        )
                        );

        if (!flightSchedule.isActive()) {

            throw new RuntimeException(
                    "Flight schedule is inactive."
            );
        }

        FlightFare flightFare =
                flightFareRepository
                        .findByFlightScheduleIdAndFareClassForUpdate(
                                flightSchedule.getId(),
                                request.getFareClass()
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Fare not found for flight "
                                                        + flightSchedule.getFlightNumber()
                                                        + " and class "
                                                        + request.getFareClass()
                                        )
                        );

        if (!flightFare.isActive()) {

            throw new RuntimeException(
                    "Selected flight fare is inactive."
            );
        }

        if (
                request.getPassengers() == null
                        ||
                        request.getPassengers().isEmpty()
        ) {

            throw new RuntimeException(
                    "At least one passenger is required."
            );
        }

        int passengerCount =
                request
                        .getPassengers()
                        .size();

        if (
                flightFare.getAvailableSeats()
                        < passengerCount
        ) {

            throw new RuntimeException(
                    "Only "
                            + flightFare.getAvailableSeats()
                            + " seats are available for "
                            + flightFare.getFareClass()
                            + "."
            );
        }

        List<Seat> selectedSeats =
                validateAndGetSelectedSeats(
                        flightSchedule,
                        flightFare,
                        request.getPassengers()
                );

        BigDecimal totalAmount =
                flightFare
                        .getBaseFare()
                        .multiply(
                                BigDecimal.valueOf(
                                        passengerCount
                                )
                        );

        Booking booking =
                Booking.builder()
                        .bookingReference(
                                generateBookingReference()
                        )
                        .user(user)
                        .flightSchedule(
                                flightSchedule
                        )
                        .flightFare(
                                flightFare
                        )
                        .passengerCount(
                                passengerCount
                        )
                        .totalAmount(
                                totalAmount
                        )
                        .status(
                                BookingStatus.CONFIRMED
                        )
                        .active(true)
                        .build();

        Booking savedBooking =
                bookingRepository.save(
                        booking
                );

        List<Passenger> passengers =
                new ArrayList<>();

        for (
                int passengerIndex = 0;
                passengerIndex < passengerCount;
                passengerIndex++
        ) {

            PassengerRequest passengerRequest =
                    request
                            .getPassengers()
                            .get(
                                    passengerIndex
                            );

            Seat selectedSeat =
                    selectedSeats.get(
                            passengerIndex
                    );

            Passenger passenger =
                    buildPassenger(
                            savedBooking,
                            passengerRequest,
                            selectedSeat.getSeatNumber()
                    );

            passengers.add(
                    passenger
            );
        }

        List<Passenger> savedPassengers =
                passengerRepository.saveAll(
                        passengers
                );

        for (Seat selectedSeat : selectedSeats) {

            seatService.markSeatBooked(
                    selectedSeat
            );
        }

        flightFare.setAvailableSeats(
                flightFare.getAvailableSeats()
                        - passengerCount
        );

        flightFareRepository.save(
                flightFare
        );

        return mapToBookingResponse(
                savedBooking,
                savedPassengers
        );
    }

    @Transactional
    public BookingResponse cancelBooking(
            String bookingReference
    ) {

        if (
                bookingReference == null
                        ||
                        bookingReference.isBlank()
        ) {

            throw new RuntimeException(
                    "Booking reference is required."
            );
        }

        User user =
                getCurrentAuthenticatedUser();

        Booking booking =
                bookingRepository
                        .findByBookingReferenceIgnoreCase(
                                bookingReference.trim()
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Booking not found for reference: "
                                                        + bookingReference
                                        )
                        );

        if (
                !booking
                        .getUser()
                        .getId()
                        .equals(
                                user.getId()
                        )
        ) {

            throw new RuntimeException(
                    "You are not authorized to cancel this booking."
            );
        }

        if (
                booking.getStatus()
                        == BookingStatus.CANCELLED
        ) {

            throw new RuntimeException(
                    "Booking is already cancelled."
            );
        }

        FlightFare flightFare =
                flightFareRepository
                        .findByFlightScheduleIdAndFareClassForUpdate(
                                booking
                                        .getFlightSchedule()
                                        .getId(),

                                booking
                                        .getFlightFare()
                                        .getFareClass()
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Flight fare could not be locked for cancellation."
                                        )
                        );

        List<Passenger> passengers =
                passengerRepository
                        .findByBookingOrderByIdAsc(
                                booking
                        );

        for (Passenger passenger : passengers) {

            seatService.releaseSeat(
                    booking.getFlightSchedule(),
                    passenger.getSeatNumber()
            );

            passenger.setActive(false);
        }

        List<Passenger> savedPassengers =
                passengerRepository.saveAll(
                        passengers
                );

        flightFare.setAvailableSeats(
                flightFare.getAvailableSeats()
                        + booking.getPassengerCount()
        );

        flightFareRepository.save(
                flightFare
        );

        booking.setStatus(
                BookingStatus.CANCELLED
        );

        booking.setActive(false);

        Booking savedBooking =
                bookingRepository.save(
                        booking
                );

        return mapToBookingResponse(
                savedBooking,
                savedPassengers
        );
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {

        User user =
                getCurrentAuthenticatedUser();

        return bookingRepository
                .findByUserOrderByCreatedAtDesc(
                        user
                )
                .stream()
                .map(
                        booking ->
                                mapToBookingResponse(
                                        booking,

                                        passengerRepository
                                                .findByBookingOrderByIdAsc(
                                                        booking
                                                )
                                )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(
            String bookingReference
    ) {

        if (
                bookingReference == null
                        ||
                        bookingReference.isBlank()
        ) {

            throw new RuntimeException(
                    "Booking reference is required."
            );
        }

        User user =
                getCurrentAuthenticatedUser();

        Booking booking =
                bookingRepository
                        .findByBookingReferenceIgnoreCase(
                                bookingReference.trim()
                        )
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Booking not found for reference: "
                                                        + bookingReference
                                        )
                        );

        if (
                !booking
                        .getUser()
                        .getId()
                        .equals(
                                user.getId()
                        )
        ) {

            throw new RuntimeException(
                    "You are not authorized to access this booking."
            );
        }

        List<Passenger> passengers =
                passengerRepository
                        .findByBookingOrderByIdAsc(
                                booking
                        );

        return mapToBookingResponse(
                booking,
                passengers
        );
    }

    private List<Seat> validateAndGetSelectedSeats(
            FlightSchedule flightSchedule,
            FlightFare flightFare,
            List<PassengerRequest> passengerRequests
    ) {

        Set<String> requestedSeatNumbers =
                new HashSet<>();

        List<Seat> selectedSeats =
                new ArrayList<>();

        for (
                PassengerRequest passengerRequest
                : passengerRequests
        ) {

            if (
                    passengerRequest.getSeatNumber() == null
                            ||
                            passengerRequest
                                    .getSeatNumber()
                                    .isBlank()
            ) {

                throw new RuntimeException(
                        "Seat number is required for every passenger."
                );
            }

            String seatNumber =
                    passengerRequest
                            .getSeatNumber()
                            .trim()
                            .toUpperCase();

            if (
                    !requestedSeatNumbers.add(
                            seatNumber
                    )
            ) {

                throw new RuntimeException(
                        "Seat "
                                + seatNumber
                                + " has been selected more than once."
                );
            }

            Seat seat;

            try {

                seat =
                        seatService.getAvailableSeat(
                                flightSchedule,
                                flightFare.getFareClass(),
                                seatNumber
                        );

            } catch (IllegalArgumentException |
                     IllegalStateException exception) {

                throw new RuntimeException(
                        exception.getMessage()
                );
            }

            selectedSeats.add(
                    seat
            );
        }

        return selectedSeats;
    }

    private User getCurrentAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null
                        ||
                        !authentication.isAuthenticated()
                        ||
                        authentication.getName() == null
                        ||
                        authentication
                                .getName()
                                .isBlank()
                        ||
                        "anonymousUser"
                                .equalsIgnoreCase(
                                        authentication.getName()
                                )
        ) {

            throw new RuntimeException(
                    "Authenticated user is required."
            );
        }

        String email =
                authentication
                        .getName()
                        .trim();

        return userRepository
                .findByEmail(
                        email
                )
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "Authenticated user not found: "
                                                + email
                                )
                );
    }

    private Passenger buildPassenger(
            Booking booking,
            PassengerRequest request,
            String seatNumber
    ) {

        return Passenger.builder()
                .booking(
                        booking
                )
                .firstName(
                        request
                                .getFirstName()
                                .trim()
                )
                .lastName(
                        request
                                .getLastName()
                                .trim()
                )
                .dateOfBirth(
                        request.getDateOfBirth()
                )
                .gender(
                        request
                                .getGender()
                                .trim()
                                .toUpperCase()
                )
                .passportNumber(
                        normalizeOptionalText(
                                request.getPassportNumber()
                        )
                )
                .nationality(
                        request
                                .getNationality()
                                .trim()
                                .toUpperCase()
                )
                .seatNumber(
                        seatNumber
                )
                .active(true)
                .build();
    }

    private String normalizeOptionalText(
            String value
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return null;
        }

        return value
                .trim()
                .toUpperCase();
    }

    private String generateBookingReference() {

        String bookingReference;

        do {

            bookingReference =
                    "SKY"
                            + UUID
                            .randomUUID()
                            .toString()
                            .replace(
                                    "-",
                                    ""
                            )
                            .substring(
                                    0,
                                    9
                            )
                            .toUpperCase();

        } while (
                bookingRepository
                        .existsByBookingReferenceIgnoreCase(
                                bookingReference
                        )
        );

        return bookingReference;
    }

    private BookingResponse mapToBookingResponse(
            Booking booking,
            List<Passenger> passengers
    ) {

        FlightSchedule flightSchedule =
                booking.getFlightSchedule();

        FlightRoute flightRoute =
                flightSchedule.getFlightRoute();

        Airport sourceAirport =
                flightRoute.getSourceAirport();

        Airport destinationAirport =
                flightRoute.getDestinationAirport();

        FlightFare flightFare =
                booking.getFlightFare();

        List<PassengerResponse> passengerResponses =
                passengers
                        .stream()
                        .map(
                                this::mapToPassengerResponse
                        )
                        .toList();

        return BookingResponse.builder()
                .id(
                        booking.getId()
                )
                .bookingReference(
                        booking.getBookingReference()
                )
                .userId(
                        booking.getUser().getId()
                )
                .userEmail(
                        booking.getUser().getEmail()
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
                .sourceAirportName(
                        sourceAirport.getAirportName()
                )
                .destinationIataCode(
                        destinationAirport.getIataCode()
                )
                .destinationAirportName(
                        destinationAirport.getAirportName()
                )
                .departureTime(
                        flightSchedule.getDepartureTime()
                )
                .arrivalTime(
                        flightSchedule.getArrivalTime()
                )
                .fareClass(
                        flightFare.getFareClass()
                )
                .baseFare(
                        flightFare.getBaseFare()
                )
                .passengerCount(
                        booking.getPassengerCount()
                )
                .totalAmount(
                        booking.getTotalAmount()
                )
                .status(
                        booking.getStatus()
                )
                .active(
                        booking.isActive()
                )
                .passengers(
                        passengerResponses
                )
                .createdAt(
                        booking.getCreatedAt()
                )
                .updatedAt(
                        booking.getUpdatedAt()
                )
                .build();
    }

    private PassengerResponse mapToPassengerResponse(
            Passenger passenger
    ) {

        return PassengerResponse.builder()
                .id(
                        passenger.getId()
                )
                .firstName(
                        passenger.getFirstName()
                )
                .lastName(
                        passenger.getLastName()
                )
                .dateOfBirth(
                        passenger.getDateOfBirth()
                )
                .gender(
                        passenger.getGender()
                )
                .passportNumber(
                        passenger.getPassportNumber()
                )
                .nationality(
                        passenger.getNationality()
                )
                .seatNumber(
                        passenger.getSeatNumber()
                )
                .active(
                        passenger.isActive()
                )
                .build();
    }
}