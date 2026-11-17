package com.mistry.platform.dto;

import lombok.Getter;

@Getter
public class AdminComplaintDetailResponse {
    private ComplaintResponse complaint;
    private BookingInfoResponse booking;
    private ReviewResponse review;
    private String resolvedBy;
    private ComplaintPartiesResponse parties;

    public AdminComplaintDetailResponse(ComplaintResponse complaint, BookingInfoResponse booking,
                                        ReviewResponse review, String resolvedBy) {
        this(complaint, booking, review, resolvedBy, null);
    }

    public AdminComplaintDetailResponse(ComplaintResponse complaint, BookingInfoResponse booking,
                                        ReviewResponse review, String resolvedBy,
                                        ComplaintPartiesResponse parties) {
        this.complaint = complaint;
        this.booking = booking;
        this.review = review;
        this.resolvedBy = resolvedBy;
        this.parties = parties;
    }
}
