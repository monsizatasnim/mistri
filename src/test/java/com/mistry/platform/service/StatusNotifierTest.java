package com.mistry.platform.service;

import com.mistry.platform.entity.ComplaintStatus;
import com.mistry.platform.entity.NotificationType;
import com.mistry.platform.entity.RecipientType;
import com.mistry.platform.entity.ShopOrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StatusNotifierTest {

    @Mock
    private NotificationService notifications;

    @InjectMocks
    private StatusNotifier notifier;

    @Test
    void aBookingChangeNotifiesTheCustomer() {
        notifier.bookingChanged(2L, 5L, "CONFIRMED");

        verify(notifications).create(eq(RecipientType.CUSTOMER), eq(2L), eq(NotificationType.BOOKING_STATUS),
                eq("Booking update"), eq("Your booking #5 has been confirmed."), eq("BOOKING"), eq(5L));
    }

    @Test
    void aNewOrderNotifiesBothTheShopAndTheProvider() {
        notifier.orderPlaced(7L, 3L, 11L, "Karim Hardware", true);

        verify(notifications).create(eq(RecipientType.SHOP_OWNER), eq(3L), eq(NotificationType.ORDER_STATUS),
                eq("Urgent order received"), anyString(), eq("ORDER"), eq(11L));
        verify(notifications).create(eq(RecipientType.PROVIDER), eq(7L), eq(NotificationType.ORDER_STATUS),
                eq("Order placed"), contains("Karim Hardware"), eq("ORDER"), eq(11L));
    }

    @Test
    void aNewOrderWithoutAKnownProviderStillNotifiesTheShop() {
        notifier.orderPlaced(null, 3L, 11L, "Karim Hardware", false);

        verify(notifications).create(eq(RecipientType.SHOP_OWNER), eq(3L), eq(NotificationType.ORDER_STATUS),
                eq("New order received"), anyString(), eq("ORDER"), eq(11L));
        verify(notifications, never()).create(eq(RecipientType.PROVIDER), any(), any(), any(), any(), any(), any());
    }

    @Test
    void markingAnOrderUrgentNotifiesTheShop() {
        notifier.orderMarkedUrgent(3L, 11L);

        verify(notifications).create(eq(RecipientType.SHOP_OWNER), eq(3L), eq(NotificationType.ORDER_STATUS),
                eq("Order marked urgent"), anyString(), eq("ORDER"), eq(11L));
    }

    @Test
    void aShopDecisionNotifiesTheProvider() {
        notifier.orderStatusChanged(7L, 3L, 11L, ShopOrderStatus.ACCEPTED);

        verify(notifications).create(eq(RecipientType.PROVIDER), eq(7L), eq(NotificationType.ORDER_STATUS),
                eq("Order update"), eq("Your order #11 was accepted by the shop."), eq("ORDER"), eq(11L));
        verify(notifications, never()).create(eq(RecipientType.SHOP_OWNER), any(), any(), any(), any(), any(), any());
    }

    @Test
    void aCancelledOrderNotifiesTheShop() {
        notifier.orderStatusChanged(7L, 3L, 11L, ShopOrderStatus.CANCELLED);

        verify(notifications).create(eq(RecipientType.SHOP_OWNER), eq(3L), eq(NotificationType.ORDER_STATUS),
                eq("Order cancelled"), anyString(), eq("ORDER"), eq(11L));
        verify(notifications, never()).create(eq(RecipientType.PROVIDER), any(), any(), any(), any(), any(), any());
    }

    @Test
    void anOpenedComplaintNotifiesTheCustomer() {
        notifier.disputeChanged(2L, 9L, ComplaintStatus.SUBMITTED, null, null);

        verify(notifications).create(eq(RecipientType.CUSTOMER), eq(2L), eq(NotificationType.DISPUTE),
                eq("Complaint received"), anyString(), eq("COMPLAINT"), eq(9L));
    }

    @Test
    void anApprovedShopOwnerIsNotified() {
        notifier.verificationDecided(RecipientType.SHOP_OWNER, 4L, true, null);

        verify(notifications).create(eq(RecipientType.SHOP_OWNER), eq(4L), eq(NotificationType.VERIFICATION),
                eq("Verification approved"), anyString(), eq("VERIFICATION"), eq(4L));
    }

    @Test
    void aRejectedProviderGetsTheReason() {
        notifier.verificationDecided(RecipientType.PROVIDER, 6L, false, "Blurry NID");

        verify(notifications).create(eq(RecipientType.PROVIDER), eq(6L), eq(NotificationType.VERIFICATION),
                eq("Verification rejected"), contains("Blurry NID"), eq("VERIFICATION"), eq(6L));
    }

    @Test
    void eventsWithoutAnIdAreIgnored() {
        notifier.bookingChanged(null, 5L, "CONFIRMED");
        notifier.bookingChanged(2L, null, "CONFIRMED");
        notifier.disputeChanged(null, 9L, ComplaintStatus.SUBMITTED, null, null);
        notifier.verificationDecided(RecipientType.PROVIDER, null, true, null);

        verifyNoInteractions(notifications);
    }
}
