package com.mistry.platform.dto;

import com.mistry.platform.entity.OrderTerm;

import java.math.BigDecimal;

public record ShopOrderItemResponse(
        Long productId,
        String productName,
        OrderTerm term,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}