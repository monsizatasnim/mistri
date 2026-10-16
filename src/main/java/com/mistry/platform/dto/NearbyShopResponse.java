package com.mistry.platform.dto;

import java.util.List;

public record NearbyShopResponse(
        Long shopOwnerId,
        String shopName,
        String ownerName,
        String location,
        String contactInfo,
        List<String> matchedTerms) {
}