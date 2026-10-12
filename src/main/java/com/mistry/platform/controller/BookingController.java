package com.mistry.platform.controller;

import com.mistry.platform.dto.CreateBookingRequest;
import com.mistry.platform.entity.Booking;
import com.mistry.platform.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody CreateBookingRequest request) {
        Booking booking = bookingService.bookSlot(
                request.getCustomerId(), request.getProviderId(), request.getAppointmentTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping("/provider/{providerId}")
    public List<Booking> getProviderBookings(@PathVariable Long providerId) {
        return bookingService.getBookingsForProvider(providerId);
    }
}