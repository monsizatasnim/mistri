package com.mistry.platform.dto;

import com.mistry.platform.entity.ProductAvailabilityType;
import com.mistry.platform.entity.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private Long shopOwnerId;
    private String name;
    private String category;
    private String description;
    private String imageUrl;
    private ProductAvailabilityType availabilityType;
    private BigDecimal salePrice;
    private BigDecimal rentPrice;
    private StockStatus stockStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
