package com.mistry.platform.repository;

import com.mistry.platform.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByBookingId(Long bookingId);

    List<Review> findByProviderId(Long providerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.providerId = :providerId")
    Double findAverageRatingByProviderId(@Param("providerId") Long providerId);
}