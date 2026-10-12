package com.mistry.platform.dto;

import com.mistry.platform.entity.PlatformSettingsAudit;

import java.time.LocalDateTime;

public record SettingsAuditResponse(
        String fieldName,
        String oldValue,
        String newValue,
        String changedBy,
        LocalDateTime changedAt) {

    public static SettingsAuditResponse from(PlatformSettingsAudit a) {
        return new SettingsAuditResponse(
                a.getFieldName(), a.getOldValue(), a.getNewValue(), a.getChangedBy(), a.getChangedAt());
    }
}