package com.mistry.platform.controller;

import com.mistry.platform.dto.ServiceProviderRegisterResponse;
import com.mistry.platform.service.ServiceProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.mistry.platform.dto.ServiceProviderLoginResponse;
import com.mistry.platform.dto.ServiceProviderVerificationResponse;
import org.springframework.security.core.Authentication;
import com.mistry.platform.dto.ServiceProviderUpdateResponse;

@RestController
@RequestMapping("/api/service-provider")
public class ServiceProviderController {

    @Autowired
    private ServiceProviderService serviceProviderService;

    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public ResponseEntity<ServiceProviderRegisterResponse> register(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String nidNumber,
            @RequestParam String tradeLicenseNumber,
            @RequestParam("nidDocument") MultipartFile nidDocument,
            @RequestParam("tradeLicenseDocument") MultipartFile tradeLicenseDocument,
            @RequestParam("experienceCertificate") MultipartFile experienceCertificate
    ) {
        ServiceProviderRegisterResponse response = serviceProviderService.register(
                fullName, phone, email, password,
                nidNumber, tradeLicenseNumber,
                nidDocument, tradeLicenseDocument, experienceCertificate
        );
        return ResponseEntity.ok(response);
    }
    @PostMapping("/login")
    public ResponseEntity<ServiceProviderLoginResponse> login(
            @RequestParam String email,
            @RequestParam String password
    )
    {
        ServiceProviderLoginResponse response = serviceProviderService.login(email, password);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/verification-status")
    public ResponseEntity<ServiceProviderVerificationResponse> getVerificationStatus(Authentication authentication) {
        String email = authentication.getName();
        ServiceProviderVerificationResponse response = serviceProviderService.getVerificationStatus(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<ServiceProviderUpdateResponse> updateProfile(
            Authentication authentication,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String serviceCategory,
            @RequestParam(required = false) String serviceDescription,
            @RequestParam(required = false) Double priceMin,
            @RequestParam(required = false) Double priceMax,
            @RequestParam(required = false) String priceNote
    ) {
        String email = authentication.getName();
        ServiceProviderUpdateResponse response = serviceProviderService.updateProfile(
                email, fullName, phone, serviceCategory, serviceDescription, priceMin, priceMax, priceNote
        );
        return ResponseEntity.ok(response);
    }
}