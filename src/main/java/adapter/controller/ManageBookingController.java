package adapter.controller;

import domain.entity.Booking;
import usecase.ManageBookingUseCase;
import java.time.LocalDateTime;
import java.util.List;

public class ManageBookingController {
    private final ManageBookingUseCase manageBookingUseCase;

    public ManageBookingController(ManageBookingUseCase manageBookingUseCase) {
        this.manageBookingUseCase = manageBookingUseCase;
    }

    public List<Booking> getPendingRequests(String providerId) {
        return manageBookingUseCase.getPendingBookings(providerId);
    }

    public boolean accept(String bookingId) {
        return manageBookingUseCase.acceptBooking(bookingId);
    }

    public boolean decline(String bookingId) {
        return manageBookingUseCase.declineBooking(bookingId);
    }

    public boolean suggestAlternative(String bookingId, LocalDateTime newTime) {
        return manageBookingUseCase.suggestAlternativeTime(bookingId, newTime);
    }
}
