package com.mistry.platform.controller;

import com.mistry.platform.entity.ShopOrderPayment;
import com.mistry.platform.service.ShopOrderPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/service-provider")
public class ShopOrderPaymentController {

    @Autowired
    private ShopOrderPaymentService service;

    @GetMapping("/payment-methods")
    public ResponseEntity<List<String>> methods() {
        return ResponseEntity.ok(service.methods());
    }

    @GetMapping("/orders/{orderId}/payment")
    public ResponseEntity<Map<String, Object>> summary(Authentication auth, @PathVariable Long orderId) {
        return ResponseEntity.ok(service.summary(auth.getName(), orderId));
    }

    @PostMapping("/orders/{orderId}/payment")
    public ResponseEntity<ShopOrderPayment> pay(Authentication auth, @PathVariable Long orderId,
                                                @RequestParam String method) {
        return ResponseEntity.ok(service.pay(auth.getName(), orderId, method));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<ShopOrderPayment>> history(Authentication auth) {
        return ResponseEntity.ok(service.history(auth.getName()));
    }
}