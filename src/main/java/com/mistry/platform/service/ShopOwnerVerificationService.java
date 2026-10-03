package com.mistry.platform.service;

import com.mistry.platform.dto.ShopOwnerProfileResponse;
import com.mistry.platform.dto.VerificationDecisionRequest;
import com.mistry.platform.entity.ShopOwner;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.InvalidVerificationActionException;
import com.mistry.platform.exception.VerificationNotFoundException;
import com.mistry.platform.repository.ShopOwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopOwnerVerificationService {

    private final ShopOwnerRepository shopOwnerRepository;

    private ShopOwnerProfileResponse toResponse(ShopOwner shopOwner) {
        return new ShopOwnerProfileResponse(
                shopOwner.getId(),
                shopOwner.getOwnerName(),
                shopOwner.getEmail(),
                shopOwner.getPhone(),
                shopOwner.getShopName(),
                shopOwner.getLocation(),
                shopOwner.getContactInfo(),
                shopOwner.getVerificationStatus(),
                shopOwner.getAccountStatus());
    }

    public List<ShopOwnerProfileResponse> getPendingSubmissions() {
        return shopOwnerRepository.findByVerificationStatus(VerificationStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ShopOwner findOrThrow(Long id) {
        return shopOwnerRepository.findById(id)
                .orElseThrow(() -> new VerificationNotFoundException(
                        "No shop owner found with id: " + id));
    }

    public ShopOwnerProfileResponse approve(Long id) {
        ShopOwner shopOwner = findOrThrow(id);

        if (shopOwner.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new InvalidVerificationActionException(
                    "Only a pending submission can be approved. Current status: "
                            + shopOwner.getVerificationStatus());
        }

        shopOwner.setVerificationStatus(VerificationStatus.VERIFIED);
        shopOwner.setRejectionReason(null);
        ShopOwner saved = shopOwnerRepository.save(shopOwner);

        return toResponse(saved);
    }

    public ShopOwnerProfileResponse reject(Long id, VerificationDecisionRequest request) {
        ShopOwner shopOwner = findOrThrow(id);

        if (shopOwner.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new InvalidVerificationActionException(
                    "Only a pending submission can be rejected. Current status: "
                            + shopOwner.getVerificationStatus());
        }

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new InvalidVerificationActionException("A rejection reason is required.");
        }

        shopOwner.setVerificationStatus(VerificationStatus.REJECTED);
        shopOwner.setRejectionReason(request.getReason());
        ShopOwner saved = shopOwnerRepository.save(shopOwner);

        return toResponse(saved);
    }
}
