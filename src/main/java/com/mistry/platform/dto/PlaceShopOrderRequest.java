package com.mistry.platform.dto;

import java.util.List;

public record PlaceShopOrderRequest(
        Long shopOwnerId,
        Boolean urgent,
        String deliveryLocation,
        String note,
        List<ShopOrderItemRequest> items) {
}