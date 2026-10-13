package com.mistry.platform.repository;

import com.mistry.platform.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProviderId(Long providerId);

    Optional<Review> findByBookingId(Long bookingId);
}