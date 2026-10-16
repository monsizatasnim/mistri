package com.mistry.platform.service;

import com.mistry.platform.dto.NearbyShopResponse;
import com.mistry.platform.dto.ShopInfoResponse;
import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.ShopOwner;
import com.mistry.platform.entity.VerificationStatus;
import com.mistry.platform.exception.ShopOrderException;
import com.mistry.platform.repository.ShopOwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShopDiscoveryService {

    // generic address words that say nothing about the area
    private static final Set<String> IGNORED_WORDS = Set.of(
            "road", "street", "lane", "house", "block", "sector", "floor",
            "near", "opposite", "bangladesh", "the", "and");

    private final ShopOwnerRepository shopOwnerRepository;

    private record Scored(ShopOwner shop, List<String> matched) {
    }

    /** Shops whose location shares words with the job location; best match first. */
    @Transactional(readOnly = true)
    public List<NearbyShopResponse> findNearby(String jobLocation) {
        Set<String> wanted = words(jobLocation);
        if (wanted.isEmpty()) {
            throw new ShopOrderException(HttpStatus.BAD_REQUEST,
                    "Job location is required (for example: Mirpur, Dhaka).");
        }

        return openShops().stream()
                .map(shop -> {
                    Set<String> shopWords = words(shop.getLocation());
                    List<String> matched = wanted.stream().filter(shopWords::contains).sorted().toList();
                    return new Scored(shop, matched);
                })
                .filter(s -> !s.matched().isEmpty())
                .sorted(Comparator.comparingInt((Scored s) -> s.matched().size()).reversed()
                        .thenComparing(s -> s.shop().getShopName(), String.CASE_INSENSITIVE_ORDER))
                .map(s -> new NearbyShopResponse(
                        s.shop().getId(),
                        s.shop().getShopName(),
                        s.shop().getOwnerName(),
                        s.shop().getLocation(),
                        s.shop().getContactInfo(),
                        s.matched()))
                .toList();
    }

    /** Locations of all open shops, so a provider can pick one instead of typing. */
    @Transactional(readOnly = true)
    public List<String> listLocations() {
        return openShops().stream()
                .map(ShopOwner::getLocation)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShopInfoResponse getShop(Long shopOwnerId) {
        ShopOwner shop = shopOwnerRepository.findById(shopOwnerId)
                .filter(this::isOpen)
                .orElseThrow(() -> new ShopOrderException(HttpStatus.NOT_FOUND, "Shop not found."));
        return new ShopInfoResponse(shop.getId(), shop.getShopName(), shop.getOwnerName(),
                shop.getLocation(), shop.getContactInfo());
    }

    // ------------------------------------------------------------ helpers

    private List<ShopOwner> openShops() {
        return shopOwnerRepository.findByVerificationStatus(VerificationStatus.VERIFIED).stream()
                .filter(this::isOpen)
                .toList();
    }

    private boolean isOpen(ShopOwner shop) {
        return shop.getVerificationStatus() == VerificationStatus.VERIFIED
                && shop.getAccountStatus() == AccountStatus.ACTIVE;
    }

    private Set<String> words(String text) {
        Set<String> result = new LinkedHashSet<>();
        if (text == null) {
            return result;
        }
        for (String word : text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) {
            if (word.length() >= 3 && !IGNORED_WORDS.contains(word)) {
                result.add(word);
            }
        }
        return result;
    }
}