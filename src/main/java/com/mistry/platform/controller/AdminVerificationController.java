package com.mistry.platform.controller;

import com.mistry.platform.dto.ServiceProviderVerificationResponse;
import com.mistry.platform.dto.VerificationDecisionRequest;
import com.mistry.platform.service.VerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/verifications")
public class AdminVerificationController {

    private final VerificationService verificationService;

    public AdminVerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ServiceProviderVerificationResponse>> getPending() {
        return ResponseEntity.ok(verificationService.getPendingSubmissions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceProviderVerificationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(verificationService.getSubmissionById(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ServiceProviderVerificationResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(verificationService.approve(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ServiceProviderVerificationResponse> reject(
            @PathVariable Long id,
            @RequestBody VerificationDecisionRequest request) {
        return ResponseEntity.ok(verificationService.reject(id, request));
    }
}