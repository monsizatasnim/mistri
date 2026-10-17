package com.mistry.platform.repository;

import com.mistry.platform.entity.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopOwnerOrderRepository extends JpaRepository<ShopOrder, Long> {

    List<ShopOrder> findByShopOwnerIdOrderByCreatedAtDesc(Long shopOwnerId);

    Optional<ShopOrder> findByIdAndShopOwnerId(Long id, Long shopOwnerId);
}
