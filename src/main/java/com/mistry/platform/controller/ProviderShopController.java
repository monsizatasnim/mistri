package com.mistry.platform.controller;

import com.mistry.platform.dto.NearbyShopResponse;
import com.mistry.platform.dto.PlaceShopOrderRequest;
import com.mistry.platform.dto.ShopInfoResponse;
import com.mistry.platform.dto.ShopOrderResponse;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.service.ShopDiscoveryService;
import com.mistry.platform.service.ShopOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProviderShopController {

    private final ShopDiscoveryService shopDiscoveryService;
    private final ShopOrderService shopOrderService;

    // ---------------------------------------------------------- finding shops

    @GetMapping("/api/shops/locations")
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
    public List<String> shopLocations() {
        return shopDiscoveryService.listLocations();
    }

    @GetMapping("/api/shops/nearby")
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
    public List<NearbyShopResponse> nearbyShops(@RequestParam(required = false) String location) {
        return shopDiscoveryService.findNearby(location);
    }

    @GetMapping("/api/shops/{shopOwnerId}")
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
    public ShopInfoResponse shopInfo(@PathVariable Long shopOwnerId) {
        return shopDiscoveryService.getShop(shopOwnerId);
    }

    // ---------------------------------------------------------- orders

    @PostMapping("/api/provider/orders")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ShopOrderResponse> placeOrder(Authentication authentication,
                                                        @RequestBody PlaceShopOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shopOrderService.placeOrder(authentication.getName(), request));
    }

    @GetMapping("/api/provider/orders")
    @PreAuthorize("hasRole('PROVIDER')")
    public List<ShopOrderResponse> myOrders(Authentication authentication) {
        return shopOrderService.myOrders(authentication.getName());
    }

    @GetMapping("/api/provider/orders/{orderId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ShopOrderResponse myOrder(Authentication authentication, @PathVariable Long orderId) {
        return shopOrderService.myOrder(authentication.getName(), orderId);
    }

    @PatchMapping("/api/provider/orders/{orderId}/urgent")
    @PreAuthorize("hasRole('PROVIDER')")
    public ShopOrderResponse markUrgent(Authentication authentication, @PathVariable Long orderId) {
        return shopOrderService.markUrgent(authentication.getName(), orderId);
    }

    @ExceptionHandler(ShopOrderException.class)
    public ResponseEntity<Map<String, String>> handle(ShopOrderException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
    }
}