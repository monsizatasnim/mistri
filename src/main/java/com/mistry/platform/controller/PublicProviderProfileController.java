package com.mistry.platform.controller;

import com.mistry.platform.dto.PublicProviderProfileResponse;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.repository.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PublicProviderProfileController {

    @Autowired
    private ServiceProviderRepository providerRepository;

    @GetMapping("/api/providers/{id}/profile")
    public ResponseEntity<PublicProviderProfileResponse> profile(@PathVariable Long id) {
        ServiceProvider p = providerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));
        return ResponseEntity.ok(new PublicProviderProfileResponse(
                p.getId(), p.getFullName(), p.getServiceCategory(), p.getServiceDescription(),
                p.getServiceLocation(), p.getPriceMin(), p.getPriceMax(), p.getPriceNote(),
                p.getVerificationStatus() == VerificationStatus.VERIFIED));
    }
}