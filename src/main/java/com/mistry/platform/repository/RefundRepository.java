package com.mistry.platform.repository;

import com.mistry.platform.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    boolean existsByComplaintId(Long complaintId);
    Optional<Refund> findByComplaintId(Long complaintId);
}