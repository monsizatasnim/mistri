package com.mistry.platform.service;

import com.mistry.platform.entity.DeliveryStatus;
import com.mistry.platform.entity.Notification;
import com.mistry.platform.exception.NotificationSendException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * One delivery attempt for a notification, and the decision of what happens next:
 * sent, try again later (the wait doubles each time), or give up after the last attempt.
 * It only changes the notification; saving it is up to the caller.
 */
@Service
public class NotificationDeliveryService {

    private static final int MAX_ERROR_LENGTH = 500;

    private final NotificationSender sender;
    private final int maxAttempts;
    private final long delaySeconds;

    public NotificationDeliveryService(
            NotificationSender sender,
            @Value("${notifications.retry.max-attempts:3}") int maxAttempts,
            @Value("${notifications.retry.delay-seconds:30}") long delaySeconds) {
        this.sender = sender;
        this.maxAttempts = maxAttempts;
        this.delaySeconds = delaySeconds;
    }

    public void attempt(Notification notification, RecipientDirectory.Contact contact) {
        String channel;
        String address;
        if (contact != null && hasText(contact.phone())) {
            channel = "SMS";
            address = contact.phone();
        } else if (contact != null && hasText(contact.email())) {
            channel = "EMAIL";
            address = contact.email();
        } else {
            notification.setDeliveryChannel("NONE");
            notification.setDeliveryStatus(DeliveryStatus.GAVE_UP);
            notification.setNextAttemptAt(null);
            notification.setLastError("No phone or email on file.");
            return;
        }

        int attemptNumber = notification.getDeliveryAttempts() + 1;
        notification.setDeliveryChannel(channel);
        notification.setDeliveryAttempts(attemptNumber);

        try {
            sender.send(channel, address, notification.getTitle(), notification.getMessage(), attemptNumber);
            notification.setDeliveryStatus(DeliveryStatus.SENT);
            notification.setDeliveredAt(LocalDateTime.now());
            notification.setNextAttemptAt(null);
            notification.setLastError(null);
        } catch (NotificationSendException e) {
            notification.setLastError(shorten(e.getMessage()));
            if (attemptNumber >= maxAttempts) {
                notification.setDeliveryStatus(DeliveryStatus.GAVE_UP);
                notification.setNextAttemptAt(null);
            } else {
                notification.setDeliveryStatus(DeliveryStatus.FAILED);
                notification.setNextAttemptAt(LocalDateTime.now().plusSeconds(delaySeconds * (1L << (attemptNumber - 1))));
            }
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String shorten(String message) {
        if (message == null) {
            return "Unknown error";
        }
        return message.length() <= MAX_ERROR_LENGTH ? message : message.substring(0, MAX_ERROR_LENGTH);
    }
}
