package com.mistry.platform.repository;

import com.mistry.platform.entity.ShopOrderStatusChange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopOrderStatusChangeRepository extends JpaRepository<ShopOrderStatusChange, Long> {
    List<ShopOrderStatusChange> findByOrderIdOrderByChangedAtAscIdAsc(Long orderId);
}
