package com.mistry.platform.controller;

import com.mistry.platform.dto.BookingRequestResponse;
import com.mistry.platform.dto.CustomerBookingRequest;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.service.CustomerBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerBookingController {

    private final CustomerBookingService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingRequestResponse create(Authentication auth,
                                         @Valid @RequestBody CustomerBookingRequest request) {
        return service.create(auth.getName(), request);
    }

    @GetMapping
    public List<BookingRequestResponse> list(Authentication auth) {
        return service.list(auth.getName());
    }

    @ExceptionHandler(ShopOrderException.class)
    public ResponseEntity<Map<String, String>> handle(ShopOrderException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
    }
}
