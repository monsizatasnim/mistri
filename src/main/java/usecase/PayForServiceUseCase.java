package usecase;

import domain.entity.Payment;
import java.util.UUID;

public class PayForServiceUseCase {
    private final ProcessPaymentUseCase processPaymentUseCase;

    public PayForServiceUseCase(ProcessPaymentUseCase processPaymentUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
    }

    public Payment payForBooking(String bookingId, double amount, String paymentMethod) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID cannot be empty.");
        }

        String idempotencyKey = "PAY-BOOKING-" + bookingId + "-" + System.currentTimeMillis();
        return processPaymentUseCase.processPayment(bookingId, idempotencyKey, amount, paymentMethod, true);
    }
}
