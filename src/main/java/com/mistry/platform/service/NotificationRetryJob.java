package com.mistry.platform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Wakes up regularly and retries the notifications whose delivery failed. */
@Component
public class NotificationRetryJob {

    private static final Logger log = LoggerFactory.getLogger(NotificationRetryJob.class);

    private final NotificationService notificationService;

    public NotificationRetryJob(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelayString = "${notifications.retry.poll-ms:10000}",
            initialDelayString = "${notifications.retry.poll-ms:10000}")
    public void retryFailedNotifications() {
        try {
            int retried = notificationService.retryDue();
            if (retried > 0) {
                log.info("Retried {} failed notification(s)", retried);
            }
        } catch (RuntimeException e) {
            log.warn("Could not retry failed notifications: {}", e.getMessage());
        }
    }
}
