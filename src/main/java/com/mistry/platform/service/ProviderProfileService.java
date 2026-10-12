package com.mistry.platform.service;

import com.mistry.platform.dto.ProviderReputationResponse;
import com.mistry.platform.repository.ReferenceRepository;
import com.mistry.platform.repository.ReviewRepository;
import com.mistry.platform.repository.ServiceProviderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProviderProfileService {

    private final ReferenceRepository referenceRepository;
    private final ReviewRepository reviewRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    public ProviderProfileService(ReferenceRepository referenceRepository,
                                  ReviewRepository reviewRepository,
                                  ServiceProviderRepository serviceProviderRepository) {
        this.referenceRepository = referenceRepository;
        this.reviewRepository = reviewRepository;
        this.serviceProviderRepository = serviceProviderRepository;
    }

    public ProviderReputationResponse getReputation(Long providerId) {
        if (!serviceProviderRepository.existsById(providerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found.");
        }
        return new ProviderReputationResponse(
                providerId,
                referenceRepository.findByProviderId(providerId),
                reviewRepository.findByProviderId(providerId));
    }
}