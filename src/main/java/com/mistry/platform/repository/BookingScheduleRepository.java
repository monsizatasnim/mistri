package com.mistry.platform.repository;

import com.mistry.platform.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingScheduleRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByProviderId(Long providerId);
}