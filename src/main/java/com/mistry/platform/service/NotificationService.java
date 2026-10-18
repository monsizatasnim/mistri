package com.mistry.platform.service;

import com.mistry.platform.entity.DeliveryStatus;
import com.mistry.platform.entity.Notification;
import com.mistry.platform.entity.NotificationType;
import com.mistry.platform.entity.RecipientType;
import com.mistry.platform.exception.NotificationException;
import com.mistry.platform.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final RecipientDirectory directory;
    private final NotificationDeliveryService delivery;

    public NotificationService(NotificationRepository repository,
                               RecipientDirectory directory,
                               NotificationDeliveryService delivery) {
        this.repository = repository;
        this.directory = directory;
        this.delivery = delivery;
    }

    /**
     * Saves the notification for the platform inbox and makes the first (simulated) SMS or
     * email attempt. It runs in its own transaction, so a problem here can never undo the
     * booking, order, complaint or verification that caused it.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification create(RecipientType recipientType, Long recipientId, NotificationType type,
                               String title, String message, String referenceType, Long referenceId) {
        Notification notification = new Notification(
                recipientType, recipientId, type, title, message, referenceType, referenceId);
        delivery.attempt(notification, directory.contactOf(recipientType, recipientId).orElse(null));
        return repository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> list(NotificationRecipient me, boolean unreadOnly) {
        return unreadOnly
                ? repository.findTop100ByRecipientTypeAndRecipientIdAndSeenFalseOrderByCreatedAtDescIdDesc(
                        me.type(), me.id())
                : repository.findTop100ByRecipientTypeAndRecipientIdOrderByCreatedAtDescIdDesc(
                        me.type(), me.id());
    }

    @Transactional(readOnly = true)
    public long unreadCount(NotificationRecipient me) {
        return repository.countByRecipientTypeAndRecipientIdAndSeenFalse(me.type(), me.id());
    }

    @Transactional
    public Notification markRead(NotificationRecipient me, Long notificationId) {
        Notification notification = repository
                .findByIdAndRecipientTypeAndRecipientId(notificationId, me.type(), me.id())
                .orElseThrow(() -> new NotificationException(HttpStatus.NOT_FOUND, "Notification not found."));
        if (!notification.isSeen()) {
            notification.setSeen(true);
            notification.setReadAt(LocalDateTime.now());
            repository.save(notification);
        }
        return notification;
    }

    @Transactional
    public int markAllRead(NotificationRecipient me) {
        List<Notification> unread = repository.findByRecipientTypeAndRecipientIdAndSeenFalse(me.type(), me.id());
        LocalDateTime now = LocalDateTime.now();
        for (Notification notification : unread) {
            notification.setSeen(true);
            notification.setReadAt(now);
        }
        repository.saveAll(unread);
        return unread.size();
    }

    /** Tries again every notification that failed and is due for another attempt. */
    @Transactional
    public int retryDue() {
        List<Notification> due = repository.findByDeliveryStatusAndNextAttemptAtLessThanEqual(
                DeliveryStatus.FAILED, LocalDateTime.now());
        for (Notification notification : due) {
            delivery.attempt(notification,
                    directory.contactOf(notification.getRecipientType(), notification.getRecipientId()).orElse(null));
        }
        repository.saveAll(due);
        return due.size();
    }
}
