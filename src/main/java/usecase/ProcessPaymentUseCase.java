package usecase;

import domain.entity.Payment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProcessPaymentUseCase {
    private final List<Payment> paymentRepository = new ArrayList<>();
    private final Map<String, Payment> idempotencyStore = new HashMap<>();

    public Payment processPayment(String referenceId, String idempotencyKey, double amount, String paymentMethod, boolean isSuccess) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }


        if (idempotencyKey != null && idempotencyStore.containsKey(idempotencyKey)) {
            return idempotencyStore.get(idempotencyKey);
        }

        String actualIdempotencyKey = (idempotencyKey != null && !idempotencyKey.isEmpty())
                ? idempotencyKey
                : UUID.randomUUID().toString();

        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);
        String status = isSuccess ? "SUCCESS" : "FAILED";

        Payment payment = new Payment(paymentId, referenceId, actualIdempotencyKey, amount, paymentMethod, status);
        paymentRepository.add(payment);
        idempotencyStore.put(actualIdempotencyKey, payment);

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