package com.mistry.platform.controller;

import com.mistry.platform.dto.BookingRequestResponse;
import com.mistry.platform.dto.SuggestTimeRequest;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.service.ProviderBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/provider/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROVIDER')")
public class ProviderBookingController {

    private final ProviderBookingService service;

    @GetMapping
    public List<BookingRequestResponse> list(Authentication auth) {
        return service.list(auth.getName());
    }

    @PostMapping("/{id}/accept")
    public BookingRequestResponse accept(Authentication auth, @PathVariable Long id) {
        return service.accept(auth.getName(), id);
    }

    @PostMapping("/{id}/decline")
    public BookingRequestResponse decline(Authentication auth, @PathVariable Long id) {
        return service.decline(auth.getName(), id);
    }

    @PostMapping("/{id}/suggest-time")
    public BookingRequestResponse suggest(Authentication auth, @PathVariable Long id,
                                          @Valid @RequestBody SuggestTimeRequest request) {
        return service.suggestTime(auth.getName(), id, request.suggestedTime());
    }

    @PostMapping("/{id}/start")
    public BookingRequestResponse start(Authentication auth, @PathVariable Long id) {
        return service.start(auth.getName(), id);
    }

    @PostMapping("/{id}/complete")
    public BookingRequestResponse complete(Authentication auth, @PathVariable Long id) {
        return service.complete(auth.getName(), id);
    }

    @ExceptionHandler(ShopOrderException.class)
    public ResponseEntity<Map<String, String>> handle(ShopOrderException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
    }
}
