package com.mistry.platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull(message = "Status is required")
        String status,
        Boolean fastDeliveryPossible,
        @Min(value = 1, message = "Estimated time must be at least 1 minute")
        @Max(value = 10080, message = "Estimated time is too long")
        Integer estimatedMinutes) {
}
