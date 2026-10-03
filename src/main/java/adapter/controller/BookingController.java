package adapter.controller;

import domain.entity.Booking;
import usecase.BookServiceUseCase;
import java.time.LocalDateTime;
import java.util.List;

public class BookingController {
    private final BookServiceUseCase bookServiceUseCase;

    public BookingController() {
        this.bookServiceUseCase = new BookServiceUseCase();
    }

    public Booking createBooking(String customerId, String providerId, LocalDateTime appointmentTime) {
        return bookServiceUseCase.bookSlot(customerId, providerId, appointmentTime);
    }

    public List<Booking> getProviderBookings(String providerId) {
        return bookServiceUseCase.getBookingsForProvider(providerId);
    }
}