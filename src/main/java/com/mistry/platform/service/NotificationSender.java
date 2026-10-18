package com.mistry.platform.service;

/**
 * Sends one notification outside the platform. In this phase it is simulated;
 * a real SMS or email provider only has to implement this interface.
 */
public interface NotificationSender {

    /**
     * @param channel       SMS or EMAIL
     * @param address       phone number or email address
     * @param attemptNumber 1 for the first try, 2 for the first retry, and so on
     * @throws com.mistry.platform.exception.NotificationSendException when delivery fails
     */
    void send(String channel, String address, String title, String message, int attemptNumber);
}
