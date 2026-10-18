package com.mistry.platform.service;

import com.mistry.platform.entity.ComplaintStatus;
import com.mistry.platform.entity.RecipientType;
import com.mistry.platform.entity.ResolutionDecision;
import com.mistry.platform.entity.ShopOrderStatus;
import com.mistry.platform.service.NotificationMessages.Text;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationMessagesTest {

    @Test
    void bookingStatusIsWrittenInPlainWords() {
        Text text = NotificationMessages.booking(5L, "CANCELLED_FULL_REFUND");

        assertEquals("Booking update", text.title());
        assertEquals("Your booking #5 was cancelled and you get a full refund.", text.body());
    }

    @Test
    void bookingStatusIsMatchedIgnoringCase() {
        assertEquals("Your booking #5 has been confirmed.", NotificationMessages.booking(5L, "confirmed").body());
    }

    @Test
    void anUnknownBookingStatusStillGetsAReadableMessage() {
        assertEquals("Your booking #5 is now waiting for payment.",
                NotificationMessages.booking(5L, "WAITING_FOR_PAYMENT").body());
    }

    @Test
    void aMissingBookingStatusSaysThePostWasUpdated() {
        assertEquals("Your booking #5 was updated.", NotificationMessages.booking(5L, null).body());
    }

    @Test
    void anUrgentOrderIsAnnouncedAsUrgentToTheShop() {
        Text urgent = NotificationMessages.orderForShop(11L, true);
        Text normal = NotificationMessages.orderForShop(11L, false);

        assertEquals("Urgent order received", urgent.title());
        assertTrue(urgent.body().contains("marked it urgent"));
        assertEquals("New order received", normal.title());
        assertEquals("Order #11 is waiting for your response.", normal.body());
    }

    @Test
    void theProviderConfirmationNamesTheShop() {
        assertEquals("Your order #11 was sent to Karim Hardware.",
                NotificationMessages.orderForProvider(11L, "Karim Hardware").body());
        assertEquals("Your order #11 was sent to the shop.",
                NotificationMessages.orderForProvider(11L, null).body());
    }

    @Test
    void orderStatusMessagesFollowTheStatus() {
        assertEquals("Your order #11 was accepted by the shop.",
                NotificationMessages.orderStatus(11L, ShopOrderStatus.ACCEPTED).body());
        assertEquals("Your order #11 was declined by the shop.",
                NotificationMessages.orderStatus(11L, ShopOrderStatus.DECLINED).body());
        assertEquals("Your order #11 is ready.",
                NotificationMessages.orderStatus(11L, ShopOrderStatus.READY).body());
    }

    @Test
    void disputeMessagesFollowTheStatus() {
        assertEquals("Complaint received",
                NotificationMessages.dispute(9L, ComplaintStatus.SUBMITTED, null, null).title());
        assertEquals("Complaint under review",
                NotificationMessages.dispute(9L, ComplaintStatus.UNDER_REVIEW, null, null).title());
        assertEquals("Complaint closed",
                NotificationMessages.dispute(9L, ComplaintStatus.CLOSED, null, null).title());
    }

    @Test
    void aResolvedDisputeMentionsTheDecisionAndTheRefund() {
        Text text = NotificationMessages.dispute(
                9L, ComplaintStatus.RESOLVED, ResolutionDecision.REFUND_ISSUED, new BigDecimal("250.00"));

        assertEquals("Complaint resolved", text.title());
        assertTrue(text.body().contains("refund issued"));
        assertTrue(text.body().contains("BDT 250."));
    }

    @Test
    void aResolvedDisputeWithoutARefundDoesNotMentionMoney() {
        Text text = NotificationMessages.dispute(9L, ComplaintStatus.RESOLVED, ResolutionDecision.REFUND_ISSUED, null);

        assertTrue(!text.body().contains("BDT"));
    }

    @Test
    void approvingAShopOwnerTellsThemTheShopIsVisible() {
        Text shop = NotificationMessages.verification(RecipientType.SHOP_OWNER, true, null);
        Text provider = NotificationMessages.verification(RecipientType.PROVIDER, true, null);

        assertEquals("Verification approved", shop.title());
        assertTrue(shop.body().contains("visible to service providers"));
        assertEquals("Your account has been verified.", provider.body());
    }

    @Test
    void aRejectionIncludesTheReason() {
        Text text = NotificationMessages.verification(RecipientType.PROVIDER, false, "Blurry NID");

        assertEquals("Verification rejected", text.title());
        assertEquals("Your verification was not approved. Reason: Blurry NID.", text.body());
    }

    @Test
    void aVeryLongReasonIsCutSoTheMessageStaysShort() {
        Text text = NotificationMessages.verification(RecipientType.PROVIDER, false, "x".repeat(1000));

        assertTrue(text.body().length() < 300);
    }
}
