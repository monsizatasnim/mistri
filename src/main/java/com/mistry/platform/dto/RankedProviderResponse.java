package com.mistry.platform.dto;

import com.mistry.platform.entity.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RankedProviderResponse {

    private int rank;
    private Long id;
    private String fullName;
    private String phone;
    private String serviceCategory;
    private String serviceDescription;
    private String serviceLocation;
    private Double priceMin;
    private Double priceMax;
    private String priceNote;
    private VerificationStatus verificationStatus;

    // Facts the customer can compare
    private Double averageRating;      // null when the provider has no reviews yet
    private int reviewCount;
    private int upcomingBookings;      // bookings in the next 7 days

    // Final score from 0 to 100, and the four parts (each 0 to 1)
    private double score;
    private double ratingScore;
    private double closenessScore;
    private double priceScore;
    private double availabilityScore;
}
