package com.mistry.platform.repository;

import com.mistry.platform.entity.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

    List<AdminActionLog> findByPerformedAtGreaterThanEqualAndPerformedAtLessThanOrderByPerformedAtDesc(
            LocalDateTime from, LocalDateTime to);
}
