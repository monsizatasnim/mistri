package com.mistry.platform.service;

import com.mistry.platform.entity.Review;
import com.mistry.platform.repository.BookingRepository;
import com.mistry.platform.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    public Review addReview(Long bookingId, String customerName, Long providerId, Integer rating, String comment) {
        if (reviewRepository.findByBookingId(bookingId).isPresent()) {
            throw new RuntimeException("A review already exists for this booking.");
        }

        Review review = new Review(providerId, bookingId, customerName, rating, comment);
        return reviewRepository.save(review);
    }

    public Review editReview(Long reviewId, Integer newRating, String newComment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (review.getCreatedAt() != null && review.getCreatedAt().plusDays(1).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Review edit window has expired (limit: 24 hours).");
        }

        review.setRating(newRating);
        review.setComment(newComment);
        return reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        reviewRepository.delete(review);
    }

    public Double getProviderAverageRating(Long providerId) {
        Double avg = reviewRepository.findAverageRatingByProviderId(providerId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public Review replyToReview(Long reviewId, String reply) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (review.getProviderReply() != null) {
            throw new RuntimeException("Provider has already replied to this review.");
        }

        review.setProviderReply(reply);
        return reviewRepository.save(review);
    }
}