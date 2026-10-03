package com.mistry.platform.repository;

import com.mistry.platform.entity.ShopOwner;
import com.mistry.platform.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopOwnerRepository extends JpaRepository<ShopOwner, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<ShopOwner> findByEmail(String email);
    Optional<ShopOwner> findByPhone(String phone);
    List<ShopOwner> findByVerificationStatus(VerificationStatus status);
}
