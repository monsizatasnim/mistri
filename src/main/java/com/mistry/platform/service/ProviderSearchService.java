package com.mistry.platform.service;

import com.mistry.platform.dto.ProviderSearchResponse;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.AccountNotFoundException;
import com.mistry.platform.repository.ServiceProviderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProviderSearchService {

    private final ServiceProviderRepository serviceProviderRepository;

    public ProviderSearchService(ServiceProviderRepository serviceProviderRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
    }

    public List<ProviderSearchResponse> search(String category, String location) {
        return serviceProviderRepository
                .findByVerificationStatusAndAccountStatus(VerificationStatus.VERIFIED, AccountStatus.ACTIVE)
                .stream()
                .filter(p -> matches(category, p.getServiceCategory()))
                .filter(p -> matches(location, p.getServiceLocation()))
                .map(ProviderSearchResponse::new)
                .toList();
    }

    public ProviderSearchResponse getProfile(Long id) {
        ServiceProvider provider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("No provider found with id: " + id));
        return new ProviderSearchResponse(provider);
    }

    private boolean matches(String filterValue, String fieldValue) {
        if (filterValue == null || filterValue.isBlank()) {
            return true;
        }
        return fieldValue != null && fieldValue.toLowerCase().contains(filterValue.toLowerCase());
    }
}