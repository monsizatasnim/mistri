package com.mistry.platform.repository;

import com.mistry.platform.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingScheduleRepository extends JpaRepository<BookingEntity, Long> {

    List<BookingEntity> findByProviderId(Long providerId);
}
