package com.mistry.platform.service;

import com.mistry.platform.dto.ReportPeriod;
import com.mistry.platform.exception.InvalidReportRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportPeriodTest {

    @Test
    void theEndDateIsIncluded() {
        ReportPeriod period = ReportPeriod.of(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        assertEquals(LocalDateTime.of(2026, 10, 1, 0, 0), period.start());
        assertEquals(LocalDateTime.of(2026, 11, 1, 0, 0), period.endExclusive());
    }

    @Test
    void fromAfterToIsRejected() {
        assertThrows(InvalidReportRequestException.class,
                () -> ReportPeriod.of(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 10, 1)));
    }

    @Test
    void rangeLongerThanAYearIsRejected() {
        assertThrows(InvalidReportRequestException.class,
                () -> ReportPeriod.of(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 10, 31)));
    }

    @Test
    void missingDatesMeanTheLast30DaysUpToToday() {
        ReportPeriod period = ReportPeriod.of(null, null);

        assertEquals(LocalDate.now(), period.to());
        assertEquals(29, ChronoUnit.DAYS.between(period.from(), period.to()));
    }

    @Test
    void onlyToGivenStartsThirtyDaysBefore() {
        ReportPeriod period = ReportPeriod.of(null, LocalDate.of(2026, 10, 31));

        assertEquals(LocalDate.of(2026, 10, 2), period.from());
    }

    @Test
    void aSingleDayIsAValidRange() {
        ReportPeriod period = ReportPeriod.of(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 5));

        assertEquals(LocalDateTime.of(2026, 10, 6, 0, 0), period.endExclusive());
    }
}