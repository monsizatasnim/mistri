package com.mistry.platform.service;

import com.mistry.platform.dto.ReportPeriod;
import com.mistry.platform.dto.SummaryReportResponse;
import com.mistry.platform.dto.SummaryReportResponse.BookingSummary;
import com.mistry.platform.dto.SummaryReportResponse.DisputeSummary;
import com.mistry.platform.dto.SummaryReportResponse.ProviderRatingEntry;
import com.mistry.platform.dto.SummaryReportResponse.RatingSummary;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.repository.ReportQueryRepository;
import com.mistry.platform.repository.ReportQueryRepository.ProviderRating;
import com.mistry.platform.repository.ReportQueryRepository.RatingStats;
import com.mistry.platform.repository.ServiceProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminReportService {

    private final ReportQueryRepository queries;
    private final ServiceProviderRepository serviceProviderRepository;

    public AdminReportService(ReportQueryRepository queries,
                              ServiceProviderRepository serviceProviderRepository) {
        this.queries = queries;
        this.serviceProviderRepository = serviceProviderRepository;
    }

    @Transactional(readOnly = true)
    public SummaryReportResponse summary(ReportPeriod period) {
        LocalDateTime from = period.start();
        LocalDateTime to = period.endExclusive();
        return new SummaryReportResponse(period.from(), period.to(),
                bookings(from, to), ratings(from, to), disputes(from, to));
    }

    private BookingSummary bookings(LocalDateTime from, LocalDateTime to) {
        Map<String, Long> byStatus = queries.bookingCountsByStatus(from, to);
        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        long cancelled = byStatus.entrySet().stream()
                .filter(e -> "CANCELLED".equalsIgnoreCase(e.getKey()))
                .mapToLong(Map.Entry::getValue)
                .sum();
        double rate = (total == 0) ? 0.0 : round2(cancelled * 100.0 / total);
        return new BookingSummary(total, byStatus, cancelled, rate);
    }

    private RatingSummary ratings(LocalDateTime from, LocalDateTime to) {
        RatingStats stats = queries.ratingStats(from, to);
        List<ProviderRating> rows = queries.ratingsByProvider(from, to);

        Map<Long, String> names = new HashMap<>();
        if (!rows.isEmpty()) {
            List<Long> ids = rows.stream().map(ProviderRating::providerId).toList();
            for (ServiceProvider provider : serviceProviderRepository.findAllById(ids)) {
                names.put(provider.getId(), provider.getFullName());
            }
        }

        List<ProviderRatingEntry> entries = rows.stream()
                .map(r -> new ProviderRatingEntry(
                        r.providerId(),
                        names.get(r.providerId()) != null ? names.get(r.providerId()) : "Provider #" + r.providerId(),
                        r.reviewCount(),
                        round2(r.averageRating())))
                .toList();

        Double average = (stats.averageRating() == null) ? null : round2(stats.averageRating());
        return new RatingSummary(stats.reviewCount(), average, entries);
    }

    private DisputeSummary disputes(LocalDateTime from, LocalDateTime to) {
        Map<String, Long> byStatus = queries.complaintCountsByStatus(from, to);
        Map<String, Long> byDecision = queries.complaintCountsByDecision(from, to);
        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        BigDecimal refunds = queries.totalRefunds(from, to);
        return new DisputeSummary(total, byStatus, byDecision, refunds == null ? BigDecimal.ZERO : refunds);
    }

    private static double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
