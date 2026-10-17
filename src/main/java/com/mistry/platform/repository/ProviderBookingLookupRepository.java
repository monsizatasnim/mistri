package com.mistry.platform.repository;

import com.mistry.platform.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProviderBookingLookupRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByProviderIdAndAppointmentTimeBetween(Long providerId, LocalDateTime from, LocalDateTime to);
}