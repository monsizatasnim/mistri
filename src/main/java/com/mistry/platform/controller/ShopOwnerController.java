package com.mistry.platform.controller;

import com.mistry.platform.dto.ShopOwnerProfileResponse;
import com.mistry.platform.dto.ShopOwnerRegisterRequest;
import com.mistry.platform.dto.ShopOwnerRegisterResponse;
import com.mistry.platform.dto.ShopOwnerUpdateProfileRequest;
import com.mistry.platform.service.ShopOwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop-owner")
@RequiredArgsConstructor
public class ShopOwnerController {

    private final ShopOwnerService shopOwnerService;

    @PostMapping("/register")
    public ResponseEntity<ShopOwnerRegisterResponse> register(@Valid @RequestBody ShopOwnerRegisterRequest request) {
        ShopOwnerRegisterResponse response = shopOwnerService.register(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<ShopOwnerProfileResponse> getProfile(Authentication authentication) {
        ShopOwnerProfileResponse response = shopOwnerService.getProfile(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<ShopOwnerProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ShopOwnerUpdateProfileRequest request) {
        ShopOwnerProfileResponse response = shopOwnerService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }
}
