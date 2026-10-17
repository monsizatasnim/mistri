package com.mistry.platform.dto;

import java.math.BigDecimal;

public record ShopOwnerOrderItemResponse(
        Long itemId,
        Long productId,
        String productName,
        String term,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        boolean unavailable) {
}
