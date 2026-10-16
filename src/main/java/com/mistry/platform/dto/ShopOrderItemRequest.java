package com.mistry.platform.dto;

import com.mistry.platform.entity.OrderTerm;

public record ShopOrderItemRequest(
        Long productId,
        Integer quantity,
        OrderTerm term) {
}