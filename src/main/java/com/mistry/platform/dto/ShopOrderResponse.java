package com.mistry.platform.dto;

import com.mistry.platform.entity.ShopOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ShopOrderResponse(
        Long id,
        Long shopOwnerId,
        String shopName,
        boolean urgent,
        String deliveryLocation,
        String note,
        ShopOrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<ShopOrderItemResponse> items) {
}