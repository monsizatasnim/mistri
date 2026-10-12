package com.mistry.platform.dto;

import com.mistry.platform.entity.Reference;
import com.mistry.platform.entity.Review;

import java.util.List;

public class ProviderReputationResponse {

    private final Long providerId;
    private final List<Reference> references;
    private final List<Review> reviews;

    public ProviderReputationResponse(Long providerId, List<Reference> references, List<Review> reviews) {
        this.providerId = providerId;
        this.references = references;
        this.reviews = reviews;
    }

    public Long getProviderId() { return providerId; }
    public List<Reference> getReferences() { return references; }
    public List<Review> getReviews() { return reviews; }
}