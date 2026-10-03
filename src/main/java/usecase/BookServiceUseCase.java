package usecase;

import domain.entity.Booking;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookServiceUseCase {
    private final List<Booking> bookingRepository = new ArrayList<>();

    public Booking bookSlot(String customerId, String providerId, LocalDateTime requestedTime) {
        for (Booking existingBooking : bookingRepository) {
            if (existingBooking.getProviderId().equals(providerId) &&
                    existingBooking.getAppointmentTime().equals(requestedTime)) {
                throw new IllegalStateException("This time slot is already booked for the selected provider.");
            }
        }

        String bookingId = UUID.randomUUID().toString();
        Booking newBooking = new Booking(bookingId, customerId, providerId, requestedTime, "CONFIRMED");
        bookingRepository.add(newBooking);
        return newBooking;
    }

    public List<Booking> getBookingsForProvider(String providerId) {
        List<Booking> result = new ArrayList<>();
        for (Booking b : bookingRepository) {
            if (b.getProviderId().equals(providerId)) {
                result.add(b);
            }
        }
        return result;
    }
}