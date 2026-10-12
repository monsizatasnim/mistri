package com.mistry.platform.service;

import com.mistry.platform.entity.Booking;
import com.mistry.platform.repository.BookingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking bookSlot(Long customerId, Long providerId, LocalDateTime requestedTime) {
        if (bookingRepository.existsByProviderIdAndAppointmentTime(providerId, requestedTime)) {
            throw slotTaken();
        }
        try {
            Booking booking = new Booking(customerId, providerId, requestedTime, "CONFIRMED");
            return bookingRepository.save(booking);
        } catch (DataIntegrityViolationException e) {
            // two customers pressed book at the same moment; the DB constraint caught it
            throw slotTaken();
        }
    }

    public List<Booking> getBookingsForProvider(Long providerId) {
        return bookingRepository.findByProviderId(providerId);
    }

    private ResponseStatusException slotTaken() {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "This time slot is already booked for the selected provider.");
    }
}