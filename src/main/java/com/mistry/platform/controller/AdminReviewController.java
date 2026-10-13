package com.mistry.platform.controller;

import com.mistry.platform.dto.ReviewResponse;
import com.mistry.platform.service.AdminReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviews(
            @RequestParam(required = false) Long providerId) {
        return ResponseEntity.ok(adminReviewService.getReviews(providerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> removeReview(@PathVariable Long id) {
        adminReviewService.removeReview(id);
        return ResponseEntity.ok(Map.of("message", "Review removed"));
    }
}