package com.mistry.platform.repository;

import com.mistry.platform.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProviderBookingRequestRepository extends JpaRepository<BookingEntity, Long> {
    List<BookingEntity> findByProviderIdOrderByCreatedAtDesc(Long providerId);
    Optional<BookingEntity> findByIdAndProviderId(Long id, Long providerId);
}
