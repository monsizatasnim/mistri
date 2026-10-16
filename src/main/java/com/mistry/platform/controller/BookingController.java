package com.mistry.platform.controller;

import com.mistry.platform.dto.CreateBookingRequest;
import com.mistry.platform.entity.BookingEntity;
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
    public ResponseEntity<BookingEntity> createBooking(@RequestBody CreateBookingRequest request) {
        BookingEntity booking = bookingService.bookSlot(
                request.getCustomerId(),
                "General Service",
                "PENDING"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BookingEntity>> getCustomerBookings(@PathVariable Long customerId) {
        List<BookingEntity> bookings = bookingService.getBookingsForCustomer(customerId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping
    public ResponseEntity<List<BookingEntity>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }
    @PutMapping("/{id}/start")
    public ResponseEntity<BookingEntity> startBooking(@PathVariable Long id) {
        BookingEntity updatedBooking = bookingService.startBooking(id);
        return ResponseEntity.ok(updatedBooking);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<BookingEntity> completeBooking(@PathVariable Long id) {
        BookingEntity updatedBooking = bookingService.completeBooking(id);
        return ResponseEntity.ok(updatedBooking);
    }
}