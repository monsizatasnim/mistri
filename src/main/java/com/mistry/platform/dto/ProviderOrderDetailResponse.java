package com.mistry.platform.dto;

import com.mistry.platform.entity.ShopOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProviderOrderDetailResponse(
        Long id,
        Long shopOwnerId,
        String shopName,
        boolean urgent,
        String deliveryLocation,
        String note,
        ShopOrderStatus status,
        String declineReason,
        Boolean fastDeliveryPossible,
        Integer estimatedMinutes,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<ShopOwnerOrderItemResponse> items,
        List<OrderStatusChangeResponse> history) {
}
