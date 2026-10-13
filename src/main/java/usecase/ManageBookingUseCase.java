package usecase;

import domain.entity.Booking;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ManageBookingUseCase {
    private final List<Booking> bookingRepository;

    public ManageBookingUseCase(List<Booking> bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // View pending booking requests for a provider
    public List<Booking> getPendingBookings(String providerId) {
        List<Booking> pendingList = new ArrayList<>();
        for (Booking booking : bookingRepository) {
            if (booking.getProviderId().equals(providerId) && "PENDING".equalsIgnoreCase(booking.getStatus())) {
                pendingList.add(booking);
            }
        }
        return pendingList;
    }


    public boolean acceptBooking(String bookingId) {
        Booking targetBooking = findBookingById(bookingId);
        if (targetBooking == null) return false;

        // Check if provider already has an ACCEPTED booking at the same time
        for (Booking b : bookingRepository) {
            if (!b.getBookingId().equals(bookingId) &&
                    b.getProviderId().equals(targetBooking.getProviderId()) &&
                    "ACCEPTED".equals(b.getStatus()) &&
                    b.getAppointmentTime().equals(targetBooking.getAppointmentTime())) {
                throw new IllegalStateException("Time slot is already occupied by an accepted booking.");
            }
        }

        targetBooking.setStatus("ACCEPTED");
        return true;
    }


    public boolean declineBooking(String bookingId) {
        Booking targetBooking = findBookingById(bookingId);
        if (targetBooking == null) return false;

        targetBooking.setStatus("DECLINED");
        return true;
    }


    public boolean suggestAlternativeTime(String bookingId, LocalDateTime newTime) {
        Booking targetBooking = findBookingById(bookingId);
        if (targetBooking == null) return false;

        targetBooking.setSuggestedTime(newTime);
        targetBooking.setStatus("SUGGESTED_ALTERNATIVE");
        return true;
    }

    private Booking findBookingById(String bookingId) {
        for (Booking b : bookingRepository) {
            if (b.getBookingId().equals(bookingId)) {
                return b;
            }
        }
        return null;
    }
}
