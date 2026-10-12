package com.mistry.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminComplaintDetailResponse {
    private ComplaintResponse complaint;
    private BookingInfoResponse booking;   // null hobe jodi booking paowa na jay
    private ReviewResponse review;         // oi booking er review, na thakle null
    private String resolvedBy;
}