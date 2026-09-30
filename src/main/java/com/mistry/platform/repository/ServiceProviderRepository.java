package com.mistry.platform.repository;

import com.mistry.platform.entity.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    boolean existsByEmail(String email);
    Optional<ServiceProvider> findByEmail(String email);
}