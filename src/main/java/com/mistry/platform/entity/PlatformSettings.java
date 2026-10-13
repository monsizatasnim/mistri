package com.mistry.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "platform_settings")
@Getter
@Setter
public class PlatformSettings {

    /** There is only ever one settings row. */
    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    // ranking weights (FR-CUS-06), must add up to 1
    @Column(name = "weight_rating", nullable = false, precision = 5, scale = 4)
    private BigDecimal weightRating;

    @Column(name = "weight_distance", nullable = false, precision = 5, scale = 4)
    private BigDecimal weightDistance;

    @Column(name = "weight_price", nullable = false, precision = 5, scale = 4)
    private BigDecimal weightPrice;

    @Column(name = "weight_availability", nullable = false, precision = 5, scale = 4)
    private BigDecimal weightAvailability;

    // booking / convenience fee range in BDT (FR-CUS-35)
    @Column(name = "booking_fee_min", nullable = false, precision = 10, scale = 2)
    private BigDecimal bookingFeeMin;

    @Column(name = "booking_fee_max", nullable = false, precision = 10, scale = 2)
    private BigDecimal bookingFeeMax;

    // minimum terms every provider must follow (FR-PRO-11)
    @Column(name = "min_cancellation_window_hours", nullable = false)
    private Integer minCancellationWindowHours;

    @Column(name = "min_refund_percentage", nullable = false)
    private Integer minRefundPercentage;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Starting values, used the first time settings are read. Weights come from FR-CUS-06. */
    public static PlatformSettings defaults() {
        PlatformSettings s = new PlatformSettings();
        s.weightRating = new BigDecimal("0.4");
        s.weightDistance = new BigDecimal("0.3");
        s.weightPrice = new BigDecimal("0.2");
        s.weightAvailability = new BigDecimal("0.1");
        s.bookingFeeMin = new BigDecimal("0");
        s.bookingFeeMax = new BigDecimal("100");
        s.minCancellationWindowHours = 2;
        s.minRefundPercentage = 20;
        return s;
    }
}