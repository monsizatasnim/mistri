package com.mistry.platform.service;

import com.mistry.platform.dto.ReviewResponse;
import com.mistry.platform.entity.Review;
import com.mistry.platform.exception.ReviewNotFoundException;
import com.mistry.platform.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;

    public List<ReviewResponse> getReviews(Long providerId) {
        List<Review> reviews = (providerId == null)
                ? reviewRepository.findAll()
                : reviewRepository.findByProviderId(providerId);
        return reviews.stream().map(ReviewResponse::from).toList();
    }

    public void removeReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));
        reviewRepository.delete(review);
    }
}