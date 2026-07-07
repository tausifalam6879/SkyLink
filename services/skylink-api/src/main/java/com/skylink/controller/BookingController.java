package com.skylink.controller;

import com.skylink.dto.BookingResponse;
import com.skylink.dto.CreateBookingRequest;
import com.skylink.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin("*")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(
            BookingService bookingService
    ) {
        this.bookingService =
                bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid
            @RequestBody CreateBookingRequest request
    ) {

        BookingResponse response =
                bookingService.createBooking(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {

        List<BookingResponse> responses =
                bookingService.getMyBookings();

        return ResponseEntity.ok(
                responses
        );
    }

    @GetMapping("/{bookingReference}")
    public ResponseEntity<BookingResponse> getBookingByReference(
            @PathVariable String bookingReference
    ) {

        BookingResponse response =
                bookingService.getBookingByReference(
                        bookingReference
                );

        return ResponseEntity.ok(
                response
        );
    }

    @PostMapping("/{bookingReference}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable String bookingReference
    ) {

        BookingResponse response =
                bookingService.cancelBooking(
                        bookingReference
                );

        return ResponseEntity.ok(
                response
        );
    }
}