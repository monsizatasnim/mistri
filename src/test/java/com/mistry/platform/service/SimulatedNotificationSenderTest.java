package com.mistry.platform.service;

import com.mistry.platform.exception.NotificationSendException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimulatedNotificationSenderTest {

    @Test
    void failsOnlyForTheFirstConfiguredAttempts() {
        SimulatedNotificationSender sender = new SimulatedNotificationSender(2);

        assertThrows(NotificationSendException.class, () -> sender.send("SMS", "01711111111", "t", "m", 1));
        assertThrows(NotificationSendException.class, () -> sender.send("SMS", "01711111111", "t", "m", 2));
        assertDoesNotThrow(() -> sender.send("SMS", "01711111111", "t", "m", 3));
    }

    @Test
    void neverFailsByDefault() {
        SimulatedNotificationSender sender = new SimulatedNotificationSender(0);

        assertDoesNotThrow(() -> sender.send("EMAIL", "test@example.com", "t", "m", 1));
    }
}
