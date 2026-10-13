package com.mistry.platform.dto;

import java.math.BigDecimal;

public record PlatformSettingsRequest(
        BigDecimal weightRating,
        BigDecimal weightDistance,
        BigDecimal weightPrice,
        BigDecimal weightAvailability,
        BigDecimal bookingFeeMin,
        BigDecimal bookingFeeMax,
        Integer minCancellationWindowHours,
        Integer minRefundPercentage) {
}