package com.mistry.platform.controller;

import com.mistry.platform.dto.AddReferenceRequest;
import com.mistry.platform.dto.ProviderReputationResponse;
import com.mistry.platform.entity.Reference;
import com.mistry.platform.service.ProviderProfileService;
import com.mistry.platform.service.ReferenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ServiceProviderProfileController {

    private final ReferenceService referenceService;
    private final ProviderProfileService providerProfileService;

    public ServiceProviderProfileController(ReferenceService referenceService,
                                            ProviderProfileService providerProfileService) {
        this.referenceService = referenceService;
        this.providerProfileService = providerProfileService;
    }

    @PostMapping("/provider-profile/references")
    public ResponseEntity<Reference> addReference(@Valid @RequestBody AddReferenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(referenceService.addReference(request));
    }

    @GetMapping("/providers/{providerId}/reputation")
    public ProviderReputationResponse getReputation(@PathVariable Long providerId) {
        return providerProfileService.getReputation(providerId);
    }
}