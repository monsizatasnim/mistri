package com.mistry.platform.service;

import com.mistry.platform.entity.BookingEntity;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

    private BookingEntity find(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ShopOrderException(HttpStatus.NOT_FOUND, "Booking not found."));
    }

    private ShopOrderException conflict(String message) {
        return new ShopOrderException(HttpStatus.CONFLICT, message);
    }

    // Service Provider starts a booking
    public BookingEntity startBooking(Long id) {
        BookingEntity booking = find(id);
        String currentStatus = booking.getStatus();

        if ("CANCELLED".equalsIgnoreCase(currentStatus) || "DECLINED".equalsIgnoreCase(currentStatus)) {
            throw conflict("Cannot start a cancelled or declined booking.");
        }
        if (!"CONFIRMED".equalsIgnoreCase(currentStatus)) {
            throw conflict("Only CONFIRMED bookings can be started.");
        }

        booking.setStatus("IN_PROGRESS");
        return bookingRepository.save(booking);
    }

    // Service Provider completes a booking
    public BookingEntity completeBooking(Long id) {
        BookingEntity booking = find(id);
        String currentStatus = booking.getStatus();

        if ("CANCELLED".equalsIgnoreCase(currentStatus) || "DECLINED".equalsIgnoreCase(currentStatus)) {
            throw conflict("Cannot complete a cancelled or declined booking.");
        }
        if (!"IN_PROGRESS".equalsIgnoreCase(currentStatus)) {
            throw conflict("Only IN_PROGRESS bookings can be completed.");
        }

        booking.setStatus("COMPLETED");
        return bookingRepository.save(booking);
    }

    public BookingEntity cancelBooking(Long id) {
        BookingEntity booking = find(id);

        if ("COMPLETED".equalsIgnoreCase(booking.getStatus())) {
            throw conflict("Completed bookings cannot be cancelled.");
        }

        // Check if provider has responded/accepted
        boolean providerResponded = "ACCEPTED".equalsIgnoreCase(booking.getStatus())
                || "CONFIRMED".equalsIgnoreCase(booking.getStatus());

        if (!providerResponded) {
            // Cancelling before provider response gives a full refund
            booking.setStatus("CANCELLED_FULL_REFUND");
        } else {
            booking.setStatus("CANCELLED");
        }

        return bookingRepository.save(booking);
    }

    public BookingEntity rescheduleBooking(Long id) {
        BookingEntity booking = find(id);

        // Request a new date/time - requires provider approval
        booking.setStatus("RESCHEDULE_REQUESTED");
        return bookingRepository.save(booking);
    }
}
