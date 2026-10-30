package com.mistry.platform.repository;

import com.mistry.platform.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProviderBookingLookupRepository extends JpaRepository<BookingEntity, Long> {
    List<BookingEntity> findByProviderIdAndAppointmentTimeBetween(Long providerId, LocalDateTime from, LocalDateTime to);
}
