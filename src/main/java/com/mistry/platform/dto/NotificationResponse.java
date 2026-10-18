package com.mistry.platform.dto;

import com.mistry.platform.entity.DeliveryStatus;
import com.mistry.platform.entity.Notification;
import com.mistry.platform.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        String referenceType,
        Long referenceId,
        boolean read,
        LocalDateTime createdAt,
        DeliveryStatus deliveryStatus,
        int deliveryAttempts) {

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType(), n.getTitle(), n.getMessage(),
                n.getReferenceType(), n.getReferenceId(), n.isSeen(), n.getCreatedAt(),
                n.getDeliveryStatus(), n.getDeliveryAttempts());
    }
}
