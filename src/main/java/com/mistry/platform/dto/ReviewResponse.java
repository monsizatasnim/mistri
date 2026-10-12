package com.mistry.platform.dto;

import com.mistry.platform.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long providerId;
    private Long bookingId;
    private String customerName;
    private int rating;
    private String comment;

    public static ReviewResponse from(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getProviderId(),
                r.getBookingId(),
                r.getCustomerName(),
                r.getRating(),
                r.getComment());
    }
}