package com.mistry.platform.repository;

import com.mistry.platform.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByShopOwnerIdOrderByCreatedAtDesc(Long shopOwnerId);
    Optional<Product> findByIdAndShopOwnerId(Long id, Long shopOwnerId);
}
