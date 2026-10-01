package com.mistry.platform.service;

import com.mistry.platform.dto.ServiceProviderVerificationResponse;
import com.mistry.platform.dto.VerificationDecisionRequest;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.InvalidVerificationActionException;
import com.mistry.platform.exception.VerificationNotFoundException;
import com.mistry.platform.repository.ServiceProviderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VerificationService {

    private final ServiceProviderRepository serviceProviderRepository;

    public VerificationService(ServiceProviderRepository serviceProviderRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
    }

    public List<ServiceProviderVerificationResponse> getPendingSubmissions() {
        return serviceProviderRepository.findByVerificationStatus(VerificationStatus.PENDING)
                .stream()
                .map(ServiceProviderVerificationResponse::new)
                .toList();
    }

    public ServiceProviderVerificationResponse getSubmissionById(Long id) {
        ServiceProvider provider = findProviderOrThrow(id);
        return new ServiceProviderVerificationResponse(provider);
    }

    public ServiceProviderVerificationResponse approve(Long id) {
        ServiceProvider provider = findProviderOrThrow(id);

        if (provider.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new InvalidVerificationActionException(
                    "Only a pending submission can be approved. Current status: "
                            + provider.getVerificationStatus());
        }

        provider.setVerificationStatus(VerificationStatus.VERIFIED);
        provider.setRejectionReason(null);
        ServiceProvider saved = serviceProviderRepository.save(provider);

        return new ServiceProviderVerificationResponse(saved);
    }

    public ServiceProviderVerificationResponse reject(Long id, VerificationDecisionRequest request) {
        ServiceProvider provider = findProviderOrThrow(id);

        if (provider.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new InvalidVerificationActionException(
                    "Only a pending submission can be rejected. Current status: "
                            + provider.getVerificationStatus());
        }

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new InvalidVerificationActionException("A rejection reason is required.");
        }

        provider.setVerificationStatus(VerificationStatus.REJECTED);
        provider.setRejectionReason(request.getReason());
        ServiceProvider saved = serviceProviderRepository.save(provider);

        return new ServiceProviderVerificationResponse(saved);
    }

    private ServiceProvider findProviderOrThrow(Long id) {
        return serviceProviderRepository.findById(id)
                .orElseThrow(() -> new VerificationNotFoundException(
                        "No service provider found with id: " + id));
    }
}