package com.mistry.platform.service;

import com.mistry.platform.exception.NotificationSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Pretends to send an SMS or email by writing it to the log. The first
 * "notifications.simulate.fail-first-attempts" attempts of every notification fail, so the
 * retry can be tried without a real gateway. The default is 0, meaning nothing fails.
 */
@Component
public class SimulatedNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SimulatedNotificationSender.class);

    private final int failFirstAttempts;

    public SimulatedNotificationSender(
            @Value("${notifications.simulate.fail-first-attempts:0}") int failFirstAttempts) {
        this.failFirstAttempts = failFirstAttempts;
    }

    @Override
    public void send(String channel, String address, String title, String message, int attemptNumber) {
        if (attemptNumber <= failFirstAttempts) {
            throw new NotificationSendException(
                    "Simulated " + channel + " gateway failure on attempt " + attemptNumber);
        }
        log.info("[SIMULATED {}] to {}: {} - {}", channel, address, title, message);
    }
}
