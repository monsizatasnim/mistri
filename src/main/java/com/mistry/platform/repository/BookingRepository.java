package com.mistry.platform.repository;

import com.mistry.platform.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByProviderIdAndAppointmentTime(Long providerId, LocalDateTime appointmentTime);

    List<Booking> findByProviderId(Long providerId);
}