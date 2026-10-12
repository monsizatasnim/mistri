package adapter.controller;

import domain.entity.Payment;
import usecase.ProcessPaymentUseCase;
import java.util.List;

public class PaymentController {
    private final ProcessPaymentUseCase processPaymentUseCase;

    public PaymentController(ProcessPaymentUseCase processPaymentUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
    }

    public Payment makePayment(String referenceId, double amount, String paymentMethod, boolean simulateSuccess) {
        return processPaymentUseCase.processPayment(referenceId, amount, paymentMethod, simulateSuccess);
    }

    public List<Payment> getPayments(String referenceId) {
        return processPaymentUseCase.getPaymentHistoryByReference(referenceId);
    }
}