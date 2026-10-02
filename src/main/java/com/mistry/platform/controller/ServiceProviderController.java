package com.mistry.platform.controller;

import com.mistry.platform.dto.ServiceProviderRegisterResponse;
import com.mistry.platform.service.ServiceProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.mistry.platform.dto.ServiceProviderLoginResponse;

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
    ) {
        ServiceProviderLoginResponse response = serviceProviderService.login(email, password);
        return ResponseEntity.ok(response);
    }
}