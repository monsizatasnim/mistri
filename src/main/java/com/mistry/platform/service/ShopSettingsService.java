package com.mistry.platform.service;

import com.mistry.platform.dto.ShopSettingsRequest;
import com.mistry.platform.dto.ShopSettingsResponse;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.ShopOwner;
import com.mistry.platform.entity.ShopSettings;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.InvalidShopSettingsException;
import com.mistry.platform.exception.ProductNotFoundException;
import com.mistry.platform.repository.ShopOwnerRepository;
import com.mistry.platform.repository.ShopSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShopSettingsService {

    private final ShopSettingsRepository shopSettingsRepository;
    private final ShopOwnerRepository shopOwnerRepository;

    private ShopOwner findShopOwner(String identifier) {
        return shopOwnerRepository.findByEmail(identifier)
                .or(() -> shopOwnerRepository.findByPhone(identifier))
                .orElseThrow(() -> new BadCredentialsException("Shop owner account not found"));
    }

    // A shop that has never saved settings gets these defaults (nothing is saved).
    private ShopSettings defaultsFor(Long shopOwnerId) {
        ShopSettings settings = new ShopSettings();
        settings.setShopOwnerId(shopOwnerId);
        return settings;
    }

    private ShopSettingsResponse toResponse(ShopOwner shop, ShopSettings s) {
        return new ShopSettingsResponse(
                shop.getId(),
                shop.getShopName(),
                shop.getLocation(),
                shop.getContactInfo(),
                s.getOpensAt(),
                s.getClosesAt(),
                s.isAcceptingOnlineOrders(),
                s.getProviderDiscountPercent());
    }

    public ShopSettingsResponse getMySettings(String loggedInIdentifier) {
        ShopOwner shop = findShopOwner(loggedInIdentifier);
        ShopSettings settings = shopSettingsRepository.findByShopOwnerId(shop.getId())
                .orElseGet(() -> defaultsFor(shop.getId()));
        return toResponse(shop, settings);
    }

    public ShopSettingsResponse updateMySettings(String loggedInIdentifier, ShopSettingsRequest request) {
        ShopOwner shop = findShopOwner(loggedInIdentifier);

        boolean hasOpen = request.getOpensAt() != null;
        boolean hasClose = request.getClosesAt() != null;
        if (hasOpen != hasClose) {
            throw new InvalidShopSettingsException("Please set both the opening and the closing time, or leave both empty");
        }
        if (hasOpen && !request.getClosesAt().isAfter(request.getOpensAt())) {
            throw new InvalidShopSettingsException("Closing time must be after opening time");
        }

        ShopSettings settings = shopSettingsRepository.findByShopOwnerId(shop.getId())
                .orElseGet(() -> defaultsFor(shop.getId()));

        settings.setOpensAt(request.getOpensAt());
        settings.setClosesAt(request.getClosesAt());
        settings.setAcceptingOnlineOrders(request.getAcceptingOnlineOrders());
        settings.setProviderDiscountPercent(
                request.getProviderDiscountPercent() == null ? BigDecimal.ZERO : request.getProviderDiscountPercent());
        settings.setUpdatedAt(LocalDateTime.now());

        return toResponse(shop, shopSettingsRepository.save(settings));
    }

    // What a provider sees. Only verified, active shops are visible.
    public ShopSettingsResponse getSettingsOfShop(Long shopOwnerId) {
        ShopOwner shop = shopOwnerRepository.findById(shopOwnerId)
                .filter(s -> s.getVerificationStatus() == VerificationStatus.VERIFIED)
                .filter(s -> s.getAccountStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new ProductNotFoundException("Shop not found"));
        ShopSettings settings = shopSettingsRepository.findByShopOwnerId(shop.getId())
                .orElseGet(() -> defaultsFor(shop.getId()));
        return toResponse(shop, settings);
    }
}
