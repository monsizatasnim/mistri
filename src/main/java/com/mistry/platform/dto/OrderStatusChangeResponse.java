package com.mistry.platform.dto;

import java.time.LocalDateTime;

public record OrderStatusChangeResponse(
        String status,
        String note,
        LocalDateTime changedAt) {
}
