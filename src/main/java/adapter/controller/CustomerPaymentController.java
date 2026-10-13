package adapter.controller;

import domain.entity.Payment;
import usecase.PayForServiceUseCase;

public class CustomerPaymentController {
    private final PayForServiceUseCase payForServiceUseCase;

    public CustomerPaymentController(PayForServiceUseCase payForServiceUseCase) {
        this.payForServiceUseCase = payForServiceUseCase;
    }

    public Payment processServicePayment(String bookingId, double amount, String paymentMethod) {
        return payForServiceUseCase.payForBooking(bookingId, amount, paymentMethod);
    }
}
