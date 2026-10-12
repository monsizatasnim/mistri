package usecase;

import domain.entity.Payment;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProcessPaymentUseCase {
    private final List<Payment> paymentRepository = new ArrayList<>();

    public Payment processPayment(String referenceId, double amount, String paymentMethod, boolean isSuccess) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);
        String status = isSuccess ? "SUCCESS" : "FAILED";

        Payment payment = new Payment(paymentId, referenceId, amount, paymentMethod, status);
        paymentRepository.add(payment);

        return payment;
    }

    public List<Payment> getPaymentHistoryByReference(String referenceId) {
        List<Payment> history = new ArrayList<>();
        for (Payment p : paymentRepository) {
            if (p.getReferenceId().equals(referenceId)) {
                history.add(p);
            }
        }
        return history;
    }
}
