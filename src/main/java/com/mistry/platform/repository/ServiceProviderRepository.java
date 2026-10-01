package com.mistry.platform.repository;

import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    boolean existsByEmail(String email);
    Optional<ServiceProvider> findByEmail(String email);
    List<ServiceProvider> findByVerificationStatus(VerificationStatus status);
}