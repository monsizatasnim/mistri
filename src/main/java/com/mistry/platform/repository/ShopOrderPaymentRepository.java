package com.mistry.platform.repository;

import com.mistry.platform.entity.ShopOrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopOrderPaymentRepository extends JpaRepository<ShopOrderPayment, Long> {
    Optional<ShopOrderPayment> findByOrderId(Long orderId);
    List<ShopOrderPayment> findByProviderIdOrderByCreatedAtDesc(Long providerId);
}