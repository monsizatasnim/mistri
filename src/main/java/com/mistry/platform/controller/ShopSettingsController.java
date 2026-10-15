package com.mistry.platform.controller;

import com.mistry.platform.dto.ShopSettingsResponse;
import com.mistry.platform.service.ShopSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopSettingsController {

    private final ShopSettingsService shopSettingsService;

    @GetMapping("/{shopOwnerId}/settings")
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
    public ResponseEntity<ShopSettingsResponse> getShopSettings(@PathVariable Long shopOwnerId) {
        return ResponseEntity.ok(shopSettingsService.getSettingsOfShop(shopOwnerId));
    }
}
