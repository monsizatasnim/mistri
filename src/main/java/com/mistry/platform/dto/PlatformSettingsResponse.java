package com.mistry.platform.dto;

import com.mistry.platform.entity.PlatformSettings;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlatformSettingsResponse(
        BigDecimal weightRating,
        BigDecimal weightDistance,
        BigDecimal weightPrice,
        BigDecimal weightAvailability,
        BigDecimal bookingFeeMin,
        BigDecimal bookingFeeMax,
        Integer minCancellationWindowHours,
        Integer minRefundPercentage,
        String updatedBy,
        LocalDateTime updatedAt) {

    public static PlatformSettingsResponse from(PlatformSettings s) {
        return new PlatformSettingsResponse(
                s.getWeightRating(), s.getWeightDistance(), s.getWeightPrice(), s.getWeightAvailability(),
                s.getBookingFeeMin(), s.getBookingFeeMax(),
                s.getMinCancellationWindowHours(), s.getMinRefundPercentage(),
                s.getUpdatedBy(), s.getUpdatedAt());
    }
}