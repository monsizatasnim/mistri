package com.mistry.platform.repository;

import com.mistry.platform.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerBookingHistoryRepository extends JpaRepository<BookingEntity, Long> {
    List<BookingEntity> findByCustomerIdOrderByAppointmentTimeDesc(Long customerId);
}
