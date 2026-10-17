package com.mistry.platform.dto;

import com.mistry.platform.entity.AdminActionCategory;

import java.time.LocalDateTime;

public record AdminActionLogResponse(
        AdminActionCategory category,
        String action,
        String targetType,
        Long targetId,
        String detail,
        String adminName,
        LocalDateTime performedAt) {
}
