package com.mistry.platform.repository;

import com.mistry.platform.entity.Reference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReferenceRepository extends JpaRepository<Reference, Long> {

    List<Reference> findByProviderId(Long providerId);
}