package com.mistry.platform.controller;

import com.mistry.platform.dto.ShopOwnerProfileResponse;
import com.mistry.platform.dto.VerificationDecisionRequest;
import com.mistry.platform.service.ShopOwnerVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shop-owner-verifications")
@RequiredArgsConstructor
public class AdminShopOwnerVerificationController {

    private final ShopOwnerVerificationService shopOwnerVerificationService;

    @GetMapping("/pending")
    public ResponseEntity<List<ShopOwnerProfileResponse>> getPending() {
        return ResponseEntity.ok(shopOwnerVerificationService.getPendingSubmissions());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ShopOwnerProfileResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(shopOwnerVerificationService.approve(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ShopOwnerProfileResponse> reject(
            @PathVariable Long id,
            @RequestBody VerificationDecisionRequest request) {
        return ResponseEntity.ok(shopOwnerVerificationService.reject(id, request));
    }
}
