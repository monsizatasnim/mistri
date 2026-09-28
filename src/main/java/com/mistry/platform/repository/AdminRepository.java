package com.mistry.platform.repository;

import com.mistry.platform.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<Admin> findByEmail(String email);
}
