package com.mistry.platform.service;

import com.mistry.platform.entity.RecipientType;

/** Who a notification belongs to: a kind of user and that user's id. */
public record NotificationRecipient(RecipientType type, Long id) {
}
