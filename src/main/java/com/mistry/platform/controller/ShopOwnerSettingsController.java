package com.mistry.platform.controller;

import com.mistry.platform.dto.ShopSettingsRequest;
import com.mistry.platform.dto.ShopSettingsResponse;
import com.mistry.platform.service.ShopSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop-owner/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SHOP_OWNER')")
public class ShopOwnerSettingsController {

    private final ShopSettingsService shopSettingsService;

    @GetMapping
    public ResponseEntity<ShopSettingsResponse> getMySettings(Authentication authentication) {
        return ResponseEntity.ok(shopSettingsService.getMySettings(authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<ShopSettingsResponse> updateMySettings(
            Authentication authentication,
            @Valid @RequestBody ShopSettingsRequest request) {
        return ResponseEntity.ok(shopSettingsService.updateMySettings(authentication.getName(), request));
    }
}
