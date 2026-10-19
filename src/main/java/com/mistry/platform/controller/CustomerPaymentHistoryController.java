package com.mistry.platform.controller;

import com.mistry.platform.entity.CustomerPayment;
import com.mistry.platform.service.CustomerPaymentHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CustomerPaymentHistoryController {

    @Autowired
    private CustomerPaymentHistoryService service;

    @PostMapping("/api/customer/payments")
    public ResponseEntity<CustomerPayment> pay(
            Authentication authentication,
            @RequestParam Long bookingId,
            @RequestParam double amount,
            @RequestParam String paymentMethod,
            @RequestParam(defaultValue = "true") boolean success
    ) {
        Long id = service.customerId(authentication.getName());
        return ResponseEntity.ok(service.recordPayment(id, bookingId, amount, paymentMethod, success));
    }

    @GetMapping("/api/customer/payments")
    public ResponseEntity<List<CustomerPayment>> payments(Authentication authentication) {
        return ResponseEntity.ok(service.myPayments(service.customerId(authentication.getName())));
    }

    @GetMapping("/api/customer/payments/{paymentId}/receipt")
    public ResponseEntity<Map<String, Object>> receipt(Authentication authentication, @PathVariable Long paymentId) {
        return ResponseEntity.ok(service.receipt(service.customerId(authentication.getName()), paymentId));
    }

    @GetMapping("/api/customer/services/history")
    public ResponseEntity<List<Map<String, Object>>> history(Authentication authentication) {
        return ResponseEntity.ok(service.serviceHistory(service.customerId(authentication.getName())));
    }
}