package com.mistry.platform.service;

import com.mistry.platform.dto.ShopOwnerProfileResponse;
import com.mistry.platform.dto.ShopOwnerRegisterRequest;
import com.mistry.platform.dto.ShopOwnerRegisterResponse;
import com.mistry.platform.dto.ShopOwnerUpdateProfileRequest;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.ShopOwner;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.AccountSuspendedException;
import com.mistry.platform.exception.DuplicateAccountException;
import com.mistry.platform.repository.ShopOwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopOwnerService {

    private final ShopOwnerRepository shopOwnerRepository;
    private final PasswordEncoder passwordEncoder;

    public ShopOwnerRegisterResponse register(ShopOwnerRegisterRequest request) {

        if (shopOwnerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateAccountException("An account with this email already exists");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()
                && shopOwnerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateAccountException("This phone number is already registered");
        }

        ShopOwner shopOwner = new ShopOwner();
        shopOwner.setOwnerName(request.getOwnerName());
        shopOwner.setEmail(request.getEmail());
        shopOwner.setPhone(request.getPhone());
        shopOwner.setPassword(passwordEncoder.encode(request.getPassword()));
        shopOwner.setShopName(request.getShopName());
        shopOwner.setLocation(request.getLocation());
        shopOwner.setContactInfo(request.getContactInfo());
        shopOwner.setVerificationStatus(VerificationStatus.PENDING);

        ShopOwner saved = shopOwnerRepository.save(shopOwner);

        return new ShopOwnerRegisterResponse(
                saved.getId(),
                "Registration successful. Your shop is pending admin verification.");
    }

    private ShopOwner findByIdentifierOrThrow(String identifier) {
        return shopOwnerRepository.findByEmail(identifier)
                .or(() -> shopOwnerRepository.findByPhone(identifier))
                .orElseThrow(() -> new BadCredentialsException("Shop owner account not found"));
    }

    private ShopOwnerProfileResponse toProfileResponse(ShopOwner shopOwner) {
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

    public ShopOwnerProfileResponse getProfile(String loggedInIdentifier) {
        ShopOwner shopOwner = findByIdentifierOrThrow(loggedInIdentifier);
        return toProfileResponse(shopOwner);
    }

    public ShopOwnerProfileResponse updateProfile(String loggedInIdentifier, ShopOwnerUpdateProfileRequest request) {

        ShopOwner shopOwner = findByIdentifierOrThrow(loggedInIdentifier);

        if (!request.getEmail().equals(shopOwner.getEmail())
                && shopOwnerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateAccountException("This email is already registered to another account");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()
                && !request.getPhone().equals(shopOwner.getPhone())
                && shopOwnerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateAccountException("This phone number is already registered to another account");
        }

        shopOwner.setOwnerName(request.getOwnerName());
        shopOwner.setEmail(request.getEmail());
        shopOwner.setPhone(request.getPhone());
        shopOwner.setShopName(request.getShopName());
        shopOwner.setLocation(request.getLocation());
        shopOwner.setContactInfo(request.getContactInfo());

        ShopOwner saved = shopOwnerRepository.save(shopOwner);

        return toProfileResponse(saved);
    }
}
