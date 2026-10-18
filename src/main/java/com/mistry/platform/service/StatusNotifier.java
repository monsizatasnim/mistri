package com.mistry.platform.service;

import com.mistry.platform.entity.ComplaintStatus;
import com.mistry.platform.entity.NotificationType;
import com.mistry.platform.entity.RecipientType;
import com.mistry.platform.entity.ResolutionDecision;
import com.mistry.platform.entity.ShopOrderStatus;
import com.mistry.platform.service.NotificationMessages.Text;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * One method per event the platform tells users about. Code that changes an order status
 * later (for example the shop owner accepting an order) only needs to call
 * {@link #orderStatusChanged}.
 */
@Service
public class StatusNotifier {

    private final NotificationService notifications;

    public StatusNotifier(NotificationService notifications) {
        this.notifications = notifications;
    }

    public void bookingChanged(Long customerId, Long bookingId, String status) {
        if (customerId == null || bookingId == null) {
            return;
        }
        Text text = NotificationMessages.booking(bookingId, status);
        notifications.create(RecipientType.CUSTOMER, customerId, NotificationType.BOOKING_STATUS,
                text.title(), text.body(), "BOOKING", bookingId);
    }

    /** A new order: the shop is told to respond, the provider gets a confirmation. */
    public void orderPlaced(Long providerId, Long shopOwnerId, Long orderId, String shopName, boolean urgent) {
        if (orderId == null) {
            return;
        }
        if (shopOwnerId != null) {
            Text forShop = NotificationMessages.orderForShop(orderId, urgent);
            notifications.create(RecipientType.SHOP_OWNER, shopOwnerId, NotificationType.ORDER_STATUS,
                    forShop.title(), forShop.body(), "ORDER", orderId);
        }
        if (providerId != null) {
            Text forProvider = NotificationMessages.orderForProvider(orderId, shopName);
            notifications.create(RecipientType.PROVIDER, providerId, NotificationType.ORDER_STATUS,
                    forProvider.title(), forProvider.body(), "ORDER", orderId);
        }
    }

    public void orderMarkedUrgent(Long shopOwnerId, Long orderId) {
        if (shopOwnerId == null || orderId == null) {
            return;
        }
        Text text = NotificationMessages.orderMarkedUrgent(orderId);
        notifications.create(RecipientType.SHOP_OWNER, shopOwnerId, NotificationType.ORDER_STATUS,
                text.title(), text.body(), "ORDER", orderId);
    }

    /** The shop owner is told when the provider cancels, the provider about everything the shop does. */
    public void orderStatusChanged(Long providerId, Long shopOwnerId, Long orderId, ShopOrderStatus status) {
        if (orderId == null || status == null) {
            return;
        }
        if (status == ShopOrderStatus.CANCELLED) {
            if (shopOwnerId != null) {
                Text text = NotificationMessages.orderCancelledForShop(orderId);
                notifications.create(RecipientType.SHOP_OWNER, shopOwnerId, NotificationType.ORDER_STATUS,
                        text.title(), text.body(), "ORDER", orderId);
            }
        } else if (status != ShopOrderStatus.PLACED && providerId != null) {
            Text text = NotificationMessages.orderStatus(orderId, status);
            notifications.create(RecipientType.PROVIDER, providerId, NotificationType.ORDER_STATUS,
                    text.title(), text.body(), "ORDER", orderId);
        }
    }

    public void disputeChanged(Long customerId, Long complaintId, ComplaintStatus status,
                               ResolutionDecision decision, BigDecimal refundAmount) {
        if (customerId == null || complaintId == null) {
            return;
        }
        Text text = NotificationMessages.dispute(complaintId, status, decision, refundAmount);
        notifications.create(RecipientType.CUSTOMER, customerId, NotificationType.DISPUTE,
                text.title(), text.body(), "COMPLAINT", complaintId);
    }

    public void verificationDecided(RecipientType who, Long accountId, boolean approved, String reason) {
        if (who == null || accountId == null) {
            return;
        }
        Text text = NotificationMessages.verification(who, approved, reason);
        notifications.create(who, accountId, NotificationType.VERIFICATION,
                text.title(), text.body(), "VERIFICATION", accountId);
    }
}
