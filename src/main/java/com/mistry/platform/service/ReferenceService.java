package com.mistry.platform.service;

import com.mistry.platform.dto.AddReferenceRequest;
import com.mistry.platform.entity.Reference;
import com.mistry.platform.repository.ReferenceRepository;
import com.mistry.platform.repository.ServiceProviderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReferenceService {

    private final ReferenceRepository referenceRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    public ReferenceService(ReferenceRepository referenceRepository,
                            ServiceProviderRepository serviceProviderRepository) {
        this.referenceRepository = referenceRepository;
        this.serviceProviderRepository = serviceProviderRepository;
    }

    public Reference addReference(AddReferenceRequest request) {
        if (!serviceProviderRepository.existsById(request.getProviderId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found.");
        }
        Reference reference = new Reference(
                request.getProviderId(),
                request.getReferenceName().trim(),
                request.getContactInfo().trim(),
                request.getDescription());
        return referenceRepository.save(reference);
    }
}