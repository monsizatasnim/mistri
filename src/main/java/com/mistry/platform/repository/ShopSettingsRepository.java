package com.mistry.platform.repository;

import com.mistry.platform.entity.ShopSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopSettingsRepository extends JpaRepository<ShopSettings, Long> {
    Optional<ShopSettings> findByShopOwnerId(Long shopOwnerId);
}
