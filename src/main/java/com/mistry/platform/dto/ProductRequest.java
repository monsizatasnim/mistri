package com.mistry.platform.dto;

import com.mistry.platform.entity.ProductAvailabilityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Product name must be at most 150 characters")
    private String name;

    @Size(max = 100, message = "Category must be at most 100 characters")
    private String category;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @Size(max = 255, message = "Image link must be at most 255 characters")
    @Pattern(regexp = "^$|^https?://.+", message = "Image link must start with http:// or https://")
    private String imageUrl;

    @NotNull(message = "Availability type is required (SALE, RENT or BOTH)")
    private ProductAvailabilityType availabilityType;

    @PositiveOrZero(message = "Sale price cannot be negative")
    private BigDecimal salePrice;

    @PositiveOrZero(message = "Rent price cannot be negative")
    private BigDecimal rentPrice;
}
