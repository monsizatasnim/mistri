package com.mistry.platform.repository;

import com.mistry.platform.entity.PlatformSettingsAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatformSettingsAuditRepository extends JpaRepository<PlatformSettingsAudit, Long> {

    List<PlatformSettingsAudit> findAllByOrderByChangedAtDescIdDesc();
}