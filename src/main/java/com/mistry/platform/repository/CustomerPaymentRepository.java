package com.mistry.platform.repository;

import com.mistry.platform.entity.CustomerPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerPaymentRepository extends JpaRepository<CustomerPayment, Long> {
    List<CustomerPayment> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}