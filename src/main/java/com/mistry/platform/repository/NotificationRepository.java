package com.mistry.platform.repository;

import com.mistry.platform.entity.DeliveryStatus;
import com.mistry.platform.entity.Notification;
import com.mistry.platform.entity.RecipientType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop100ByRecipientTypeAndRecipientIdOrderByCreatedAtDescIdDesc(
            RecipientType recipientType, Long recipientId);

    List<Notification> findTop100ByRecipientTypeAndRecipientIdAndSeenFalseOrderByCreatedAtDescIdDesc(
            RecipientType recipientType, Long recipientId);

    List<Notification> findByRecipientTypeAndRecipientIdAndSeenFalse(
            RecipientType recipientType, Long recipientId);

    long countByRecipientTypeAndRecipientIdAndSeenFalse(RecipientType recipientType, Long recipientId);

    Optional<Notification> findByIdAndRecipientTypeAndRecipientId(
            Long id, RecipientType recipientType, Long recipientId);

    List<Notification> findByDeliveryStatusAndNextAttemptAtLessThanEqual(
            DeliveryStatus deliveryStatus, LocalDateTime time);
}
