package com.mistry.platform.service;

import com.mistry.platform.dto.ProviderSearchResponse;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.AccountNotFoundException;
import com.mistry.platform.exception.InvalidSearchFilterException;
import com.mistry.platform.repository.ServiceProviderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProviderSearchService {

    private final ServiceProviderRepository serviceProviderRepository;

    public ProviderSearchService(ServiceProviderRepository serviceProviderRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
    }

    public List<ProviderSearchResponse> search(String category, String location) {
        return search(category, location, null, null, null);
    }

    public List<ProviderSearchResponse> search(String category, String location,
                                               Double minPrice, Double maxPrice,
                                               String verificationStatus) {
        validatePriceRange(minPrice, maxPrice);

        return loadCandidates(verificationStatus)
                .stream()
                .filter(p -> matches(category, p.getServiceCategory()))
                .filter(p -> matches(location, p.getServiceLocation()))
                .filter(p -> withinPriceRange(p, minPrice, maxPrice))
                .map(ProviderSearchResponse::new)
                .toList();
    }

    public ProviderSearchResponse getProfile(Long id) {
        ServiceProvider provider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("No provider found with id: " + id));
        return new ProviderSearchResponse(provider);
    }

    private List<ServiceProvider> loadCandidates(String verificationStatus) {
        String value = (verificationStatus == null || verificationStatus.isBlank())
                ? "VERIFIED"
                : verificationStatus.trim().toUpperCase();

        return switch (value) {
            case "VERIFIED" -> serviceProviderRepository
                    .findByVerificationStatusAndAccountStatus(VerificationStatus.VERIFIED, AccountStatus.ACTIVE);
            case "PENDING" -> serviceProviderRepository
                    .findByVerificationStatusAndAccountStatus(VerificationStatus.PENDING, AccountStatus.ACTIVE);
            case "ALL" -> {
                List<ServiceProvider> all = new ArrayList<>(serviceProviderRepository
                        .findByVerificationStatusAndAccountStatus(VerificationStatus.VERIFIED, AccountStatus.ACTIVE));
                all.addAll(serviceProviderRepository
                        .findByVerificationStatusAndAccountStatus(VerificationStatus.PENDING, AccountStatus.ACTIVE));
                yield all;
            }
            default -> throw new InvalidSearchFilterException(
                    "Verification status must be VERIFIED, PENDING or ALL");
        };
    }

    private void validatePriceRange(Double minPrice, Double maxPrice) {
        if (minPrice != null && minPrice < 0) {
            throw new InvalidSearchFilterException("Minimum price cannot be negative");
        }
        if (maxPrice != null && maxPrice < 0) {
            throw new InvalidSearchFilterException("Maximum price cannot be negative");
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new InvalidSearchFilterException("Minimum price cannot be greater than maximum price");
        }
    }

    private boolean withinPriceRange(ServiceProvider p, Double minPrice, Double maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return true;
        }
        Double low = p.getPriceMin() != null ? p.getPriceMin() : p.getPriceMax();
        Double high = p.getPriceMax() != null ? p.getPriceMax() : p.getPriceMin();
        if (low == null || high == null) {
            return false;
        }
        double wantedLow = (minPrice == null) ? 0 : minPrice;
        double wantedHigh = (maxPrice == null) ? Double.MAX_VALUE : maxPrice;
        return high >= wantedLow && low <= wantedHigh;
    }

    private boolean matches(String filterValue, String fieldValue) {
        if (filterValue == null || filterValue.isBlank()) {
            return true;
        }
        return fieldValue != null && fieldValue.toLowerCase().contains(filterValue.toLowerCase());
    }
}
