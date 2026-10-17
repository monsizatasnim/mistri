package com.mistry.platform.controller;

import com.mistry.platform.dto.DeclineOrderRequest;
import com.mistry.platform.dto.OrderStatusUpdateRequest;
import com.mistry.platform.dto.ShopOwnerOrderResponse;
import com.mistry.platform.dto.UrgentDeliveryRequest;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.service.ShopOrderManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop-owner/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SHOP_OWNER')")
public class ShopOwnerOrderController {

    private final ShopOrderManagementService service;

    @GetMapping
    public List<ShopOwnerOrderResponse> list(Authentication auth,
                                             @RequestParam(required = false) String status) {
        return service.list(auth.getName(), status);
    }

    @GetMapping("/history")
    public List<ShopOwnerOrderResponse> history(Authentication auth) {
        return service.history(auth.getName());
    }

    @GetMapping("/{id}")
    public ShopOwnerOrderResponse get(Authentication auth, @PathVariable Long id) {
        return service.get(auth.getName(), id);
    }

    @PostMapping("/{id}/accept")
    public ShopOwnerOrderResponse accept(Authentication auth, @PathVariable Long id) {
        return service.accept(auth.getName(), id);
    }

    @PostMapping("/{id}/decline")
    public ShopOwnerOrderResponse decline(Authentication auth, @PathVariable Long id,
                                          @Valid @RequestBody DeclineOrderRequest request) {
        return service.decline(auth.getName(), id, request.reason());
    }

    @PatchMapping("/{id}/status")
    public ShopOwnerOrderResponse updateStatus(Authentication auth, @PathVariable Long id,
                                               @Valid @RequestBody OrderStatusUpdateRequest request) {
        return service.updateStatus(auth.getName(), id, request);
    }

    @PutMapping("/{id}/urgent-delivery")
    public ShopOwnerOrderResponse urgentDelivery(Authentication auth, @PathVariable Long id,
                                                 @Valid @RequestBody UrgentDeliveryRequest request) {
        return service.setUrgentDelivery(auth.getName(), id, request);
    }

    @PatchMapping("/{id}/items/{itemId}/unavailable")
    public ShopOwnerOrderResponse itemUnavailable(Authentication auth, @PathVariable Long id,
                                                  @PathVariable Long itemId) {
        return service.markItemUnavailable(auth.getName(), id, itemId);
    }

    @ExceptionHandler(ShopOrderException.class)
    public ResponseEntity<Map<String, String>> handle(ShopOrderException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
    }
}
