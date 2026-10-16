package com.mistry.platform.repository;

import com.mistry.platform.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    List<AvailabilitySlot> findByProviderIdOrderByDateAscStartTimeAsc(Long providerId);
    List<AvailabilitySlot> findByProviderIdAndDate(Long providerId, LocalDate date);
    Optional<AvailabilitySlot> findByIdAndProviderId(Long id, Long providerId);
}