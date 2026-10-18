package com.mistry.platform.exception;

/** Thrown by a sender when the SMS or email could not be delivered. */
public class NotificationSendException extends RuntimeException {

    public NotificationSendException(String message) {
        super(message);
    }
}
