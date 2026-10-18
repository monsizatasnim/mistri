package com.mistry.platform.service;

import com.mistry.platform.dto.ReportPeriod;
import com.mistry.platform.dto.SummaryReportResponse;
import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.repository.ReportQueryRepository;
import com.mistry.platform.repository.ServiceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminReportServiceTest {

    @Mock
    private ReportQueryRepository queries;
    @Mock
    private ServiceProviderRepository serviceProviderRepository;

    @InjectMocks
    private AdminReportService service;

    private final ReportPeriod period = ReportPeriod.of(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
    private final LocalDateTime start = period.start();
    private final LocalDateTime end = period.endExclusive();

    @BeforeEach
    void emptyDefaults() {
        when(queries.ratingStats(any(), any())).thenReturn(new ReportQueryRepository.RatingStats(0, null));
        when(queries.totalRefunds(any(), any())).thenReturn(BigDecimal.ZERO);
    }

    @Test
    void cancellationRateIsCancelledBookingsDividedByAllBookings() {
        when(queries.bookingCountsByStatus(start, end))
                .thenReturn(Map.of("PENDING", 6L, "CANCELLED", 2L, "COMPLETED", 2L));

        SummaryReportResponse report = service.summary(period);

        assertEquals(10, report.bookings().total());
        assertEquals(2, report.bookings().cancelled());
        assertEquals(20.0, report.bookings().cancellationRatePercent());
    }

    @Test
    void noBookingsGivesAZeroRateInsteadOfDividingByZero() {
        SummaryReportResponse report = service.summary(period);

        assertEquals(0, report.bookings().total());
        assertEquals(0.0, report.bookings().cancellationRatePercent());
    }

    @Test
    void cancelledStatusIsMatchedIgnoringCase() {
        when(queries.bookingCountsByStatus(start, end)).thenReturn(Map.of("Cancelled", 1L, "PENDING", 3L));

        SummaryReportResponse report = service.summary(period);

        assertEquals(1, report.bookings().cancelled());
        assertEquals(25.0, report.bookings().cancellationRatePercent());
    }

    @Test
    void averageRatingIsRoundedAndProviderNamesAreAdded() {
        when(queries.ratingStats(start, end)).thenReturn(new ReportQueryRepository.RatingStats(3, 4.33333));
        when(queries.ratingsByProvider(start, end)).thenReturn(List.of(
                new ReportQueryRepository.ProviderRating(7L, 2, 4.5),
                new ReportQueryRepository.ProviderRating(9L, 1, 4.0)));
        ServiceProvider rahim = new ServiceProvider();
        rahim.setId(7L);
        rahim.setFullName("Rahim");
        when(serviceProviderRepository.findAllById(any())).thenReturn(List.of(rahim));

        SummaryReportResponse report = service.summary(period);

        assertEquals(3, report.ratings().reviewCount());
        assertEquals(4.33, report.ratings().averageRating());
        assertEquals(2, report.ratings().byProvider().size());
        assertEquals("Rahim", report.ratings().byProvider().get(0).providerName());
        assertEquals("Provider #9", report.ratings().byProvider().get(1).providerName());
    }

    @Test
    void noReviewsMeansNoAverageInsteadOfZero() {
        SummaryReportResponse report = service.summary(period);

        assertEquals(0, report.ratings().reviewCount());
        assertNull(report.ratings().averageRating());
        assertTrue(report.ratings().byProvider().isEmpty());
    }

    @Test
    void disputesAreCountedByStatusAndDecisionWithTheRefundTotal() {
        when(queries.complaintCountsByStatus(start, end)).thenReturn(Map.of("SUBMITTED", 1L, "RESOLVED", 2L));
        when(queries.complaintCountsByDecision(start, end)).thenReturn(Map.of("REFUND_ISSUED", 1L, "NO_ACTION", 1L));
        when(queries.totalRefunds(start, end)).thenReturn(new BigDecimal("250.00"));

        SummaryReportResponse report = service.summary(period);

        assertEquals(3, report.disputes().total());
        assertEquals(1L, report.disputes().byDecision().get("REFUND_ISSUED"));
        assertEquals(0, new BigDecimal("250").compareTo(report.disputes().totalRefundAmount()));
    }
}