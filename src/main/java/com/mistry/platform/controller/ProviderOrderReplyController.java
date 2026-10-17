package com.mistry.platform.controller;

import com.mistry.platform.dto.ShopOrderResponse;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.service.ProviderOrderReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/provider/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROVIDER')")
public class ProviderOrderReplyController {

    private final ProviderOrderReplyService service;

    @PostMapping("/{id}/accept-partial")
    public ShopOrderResponse acceptPartial(Authentication auth, @PathVariable Long id) {
        return service.acceptPartial(auth.getName(), id);
    }

    @PostMapping("/{id}/cancel")
    public ShopOrderResponse cancel(Authentication auth, @PathVariable Long id) {
        return service.cancel(auth.getName(), id);
    }

    @ExceptionHandler(ShopOrderException.class)
    public ResponseEntity<Map<String, String>> handle(ShopOrderException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
    }
}
