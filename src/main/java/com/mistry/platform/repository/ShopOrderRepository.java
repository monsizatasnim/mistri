package com.mistry.platform.repository;

import com.mistry.platform.entity.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    List<ShopOrder> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    Optional<ShopOrder> findByIdAndProviderId(Long id, Long providerId);
}