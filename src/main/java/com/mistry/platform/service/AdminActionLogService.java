package com.mistry.platform.service;

import com.mistry.platform.dto.AdminActionLogResponse;
import com.mistry.platform.dto.ReportPeriod;
import com.mistry.platform.entity.Admin;
import com.mistry.platform.entity.AdminActionCategory;
import com.mistry.platform.entity.AdminActionLog;
import com.mistry.platform.entity.PlatformSettingsAudit;
import com.mistry.platform.repository.AdminActionLogRepository;
import com.mistry.platform.repository.AdminRepository;
import com.mistry.platform.repository.PlatformSettingsAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminActionLogService {

    private final AdminActionLogRepository logRepository;
    private final PlatformSettingsAuditRepository settingsAuditRepository;
    private final AdminRepository adminRepository;

    public AdminActionLogService(AdminActionLogRepository logRepository,
                                 PlatformSettingsAuditRepository settingsAuditRepository,
                                 AdminRepository adminRepository) {
        this.logRepository = logRepository;
        this.settingsAuditRepository = settingsAuditRepository;
        this.adminRepository = adminRepository;
    }

    /** Saves one verification or suspension action with the admin's full name. */
    public void logAction(String adminIdentifier, AdminActionCategory category, String action,
                          String targetType, Long targetId, String detail) {
        String adminName = resolveAdminName(adminIdentifier);
        logRepository.save(new AdminActionLog(
                adminName, category, action, targetType, targetId, detail, LocalDateTime.now()));
    }

    /**
     * Verification and suspension actions come from admin_action_log, settings changes from
     * the existing settings audit table. Newest first.
     */
    @Transactional(readOnly = true)
    public List<AdminActionLogResponse> getLog(ReportPeriod period, AdminActionCategory category) {
        LocalDateTime start = period.start();
        LocalDateTime end = period.endExclusive();
        List<AdminActionLogResponse> result = new ArrayList<>();

        if (category != AdminActionCategory.SETTINGS) {
            for (AdminActionLog entry : logRepository
                    .findByPerformedAtGreaterThanEqualAndPerformedAtLessThanOrderByPerformedAtDesc(start, end)) {
                if (category == null || entry.getCategory() == category) {
                    result.add(toResponse(entry));
                }
            }
        }

        if (category == null || category == AdminActionCategory.SETTINGS) {
            for (PlatformSettingsAudit change : settingsAuditRepository.findAllByOrderByChangedAtDescIdDesc()) {
                if (!change.getChangedAt().isBefore(start) && change.getChangedAt().isBefore(end)) {
                    result.add(toResponse(change));
                }
            }
        }

        result.sort(Comparator.comparing(AdminActionLogResponse::performedAt).reversed());
        return result;
    }

    private AdminActionLogResponse toResponse(AdminActionLog e) {
        return new AdminActionLogResponse(e.getCategory(), e.getAction(), e.getTargetType(),
                e.getTargetId(), e.getDetail(), e.getAdminName(), e.getPerformedAt());
    }

    private AdminActionLogResponse toResponse(PlatformSettingsAudit a) {
        String detail = a.getFieldName() + ": " + a.getOldValue() + " -> " + a.getNewValue();
        return new AdminActionLogResponse(AdminActionCategory.SETTINGS, "SETTINGS_CHANGED",
                "PLATFORM_SETTINGS", null, detail, a.getChangedBy(), a.getChangedAt());
    }

    private String resolveAdminName(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return "unknown admin";
        }
        return adminRepository.findByEmail(identifier).map(Admin::getFullName).orElse(identifier);
    }
}
