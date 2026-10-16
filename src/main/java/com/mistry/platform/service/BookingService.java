package com.mistry.platform.service;

import com.mistry.platform.entity.BookingEntity;
import com.mistry.platform.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public BookingEntity bookSlot(Long customerId, String serviceName, String status) {
        BookingEntity booking = new BookingEntity(customerId, serviceName, status);
        return bookingRepository.save(booking);
    }

    public List<BookingEntity> getBookingsForCustomer(Long customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    public List<BookingEntity> getAllBookings() {
        return bookingRepository.findAll();
    }
    // Service Provider starts a booking
    public BookingEntity startBooking(Long id) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        String currentStatus = booking.getStatus();

        if ("CANCELLED".equalsIgnoreCase(currentStatus) || "DECLINED".equalsIgnoreCase(currentStatus)) {
            throw new RuntimeException("Cannot start a cancelled or declined booking.");
        }

        if (!"CONFIRMED".equalsIgnoreCase(currentStatus)) {
            throw new RuntimeException("Only CONFIRMED bookings can be started.");
        }

        booking.setStatus("IN_PROGRESS");
        return bookingRepository.save(booking);
    }

    // Service Provider completes a booking
    public BookingEntity completeBooking(Long id) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        String currentStatus = booking.getStatus();

        if ("CANCELLED".equalsIgnoreCase(currentStatus) || "DECLINED".equalsIgnoreCase(currentStatus)) {
            throw new RuntimeException("Cannot complete a cancelled or declined booking.");
        }

        if (!"IN_PROGRESS".equalsIgnoreCase(currentStatus)) {
            throw new RuntimeException("Only IN_PROGRESS bookings can be completed.");
        }

        booking.setStatus("COMPLETED");
        return bookingRepository.save(booking);
    }
}