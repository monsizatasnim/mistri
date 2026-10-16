package com.mistry.platform.controller;

import com.mistry.platform.entity.Review;
import com.mistry.platform.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Review> addReview(
            @RequestParam Long bookingId,
            @RequestParam String customerName,
            @RequestParam Long providerId,
            @RequestParam Integer rating,
            @RequestParam String comment) {
        return ResponseEntity.ok(reviewService.addReview(bookingId, customerName, providerId, rating, comment));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> editReview(
            @PathVariable Long reviewId,
            @RequestParam Integer rating,
            @RequestParam String comment) {
        return ResponseEntity.ok(reviewService.editReview(reviewId, rating, comment));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok("Review deleted successfully");
    }

    @GetMapping("/provider/{providerId}/average-rating")
    public ResponseEntity<Double> getProviderAverageRating(@PathVariable Long providerId) {
        return ResponseEntity.ok(reviewService.getProviderAverageRating(providerId));
    }

    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<Review> replyToReview(
            @PathVariable Long reviewId,
            @RequestParam String reply) {
        return ResponseEntity.ok(reviewService.replyToReview(reviewId, reply));
    }
}