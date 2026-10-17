package com.mistry.platform.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record SummaryReportResponse(
        LocalDate from,
        LocalDate to,
        BookingSummary bookings,
        RatingSummary ratings,
        DisputeSummary disputes) {

    public record BookingSummary(
            long total,
            Map<String, Long> byStatus,
            long cancelled,
            double cancellationRatePercent) {
    }

    public record ProviderRatingEntry(
            Long providerId,
            String providerName,
            long reviewCount,
            double averageRating) {
    }

    public record RatingSummary(
            long reviewCount,
            Double averageRating,
            List<ProviderRatingEntry> byProvider) {
    }

    public record DisputeSummary(
            long total,
            Map<String, Long> byStatus,
            Map<String, Long> byDecision,
            BigDecimal totalRefundAmount) {
    }
}
