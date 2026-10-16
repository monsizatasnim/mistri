package com.mistry.platform.dto;

public record ShopInfoResponse(
        Long shopOwnerId,
        String shopName,
        String ownerName,
        String location,
        String contactInfo) {
}