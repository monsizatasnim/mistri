package com.mistry.platform.dto;

import com.mistry.platform.entity.ShopOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ShopOwnerOrderResponse(
        Long id,
        Long providerId,
        String providerName,
        String providerPhone,
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
