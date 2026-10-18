package com.mistry.platform.dto;

import com.mistry.platform.exception.InvalidReportRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record ReportPeriod(LocalDate from, LocalDate to) {

    public static final int DEFAULT_DAYS = 30;
    public static final int MAX_DAYS = 366;

    /** Missing dates mean "the last 30 days up to today". */
    public static ReportPeriod of(LocalDate from, LocalDate to) {
        LocalDate end = (to != null) ? to : LocalDate.now();
        LocalDate start = (from != null) ? from : end.minusDays(DEFAULT_DAYS - 1);

        if (start.isAfter(end)) {
            throw new InvalidReportRequestException("The 'from' date cannot be after the 'to' date.");
        }
        if (ChronoUnit.DAYS.between(start, end) >= MAX_DAYS) {
            throw new InvalidReportRequestException(
                    "The date range cannot be longer than " + MAX_DAYS + " days.");
        }
        return new ReportPeriod(start, end);
    }

    /** First moment of the first day. */
    public LocalDateTime start() {
        return from.atStartOfDay();
    }

    /** First moment after the last day, so the 'to' date itself is included. */
    public LocalDateTime endExclusive() {
        return to.plusDays(1).atStartOfDay();
    }
}
