package com.mistry.platform.repository;

import com.mistry.platform.entity.Complaint;
import com.mistry.platform.entity.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<Complaint> findByIdAndCustomerId(Long id, Long customerId);
    List<Complaint> findByStatusOrderByCreatedAtAsc(ComplaintStatus status);
}
