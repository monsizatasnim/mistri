package com.mistry.platform.service;

import com.mistry.platform.entity.DeliveryStatus;
import com.mistry.platform.entity.Notification;
import com.mistry.platform.entity.NotificationType;
import com.mistry.platform.entity.RecipientType;
import com.mistry.platform.exception.NotificationSendException;
import com.mistry.platform.service.RecipientDirectory.Contact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryServiceTest {

    @Mock
    private NotificationSender sender;

    private final Contact both = new Contact("Test", "01711111111", "test@example.com");

    private NotificationDeliveryService service() {
        // three attempts at most, 30 seconds before the first retry
        return new NotificationDeliveryService(sender, 3, 30);
    }

    private Notification notification() {
        return new Notification(RecipientType.CUSTOMER, 2L, NotificationType.BOOKING_STATUS,
                "Booking update", "Your booking #5 was received.", "BOOKING", 5L);
    }

    private void senderFails() {
        doThrow(new NotificationSendException("gateway down"))
                .when(sender).send(any(), any(), any(), any(), anyInt());
    }

    @Test
    void aSuccessfulSendMarksTheNotificationSentOnTheFirstAttempt() {
        Notification n = notification();

        service().attempt(n, both);

        assertEquals(DeliveryStatus.SENT, n.getDeliveryStatus());
        assertEquals(1, n.getDeliveryAttempts());
        assertEquals("SMS", n.getDeliveryChannel());
        assertNotNull(n.getDeliveredAt());
        assertNull(n.getNextAttemptAt());
        verify(sender).send("SMS", "01711111111", "Booking update", "Your booking #5 was received.", 1);
    }

    @Test
    void aFailedSendIsScheduledForARetryLater() {
        senderFails();
        Notification n = notification();
        LocalDateTime before = LocalDateTime.now();

        service().attempt(n, both);

        assertEquals(DeliveryStatus.FAILED, n.getDeliveryStatus());
        assertEquals(1, n.getDeliveryAttempts());
        assertEquals("gateway down", n.getLastError());
        assertTrue(n.getNextAttemptAt().isAfter(before.plusSeconds(25)));
    }

    @Test
    void theWaitBetweenRetriesDoubles() {
        senderFails();
        Notification n = notification();
        NotificationDeliveryService service = service();

        service.attempt(n, both);
        LocalDateTime beforeSecond = LocalDateTime.now();
        service.attempt(n, both);

        assertEquals(DeliveryStatus.FAILED, n.getDeliveryStatus());
        assertEquals(2, n.getDeliveryAttempts());
        assertTrue(n.getNextAttemptAt().isAfter(beforeSecond.plusSeconds(55)));
    }

    @Test
    void afterTheLastAttemptTheNotificationIsGivenUp() {
        senderFails();
        Notification n = notification();
        NotificationDeliveryService service = service();

        service.attempt(n, both);
        service.attempt(n, both);
        service.attempt(n, both);

        assertEquals(DeliveryStatus.GAVE_UP, n.getDeliveryStatus());
        assertEquals(3, n.getDeliveryAttempts());
        assertNull(n.getNextAttemptAt());
        assertEquals("gateway down", n.getLastError());
    }

    @Test
    void aRetryThatWorksClearsTheEarlierError() {
        doThrow(new NotificationSendException("gateway down")).doNothing()
                .when(sender).send(any(), any(), any(), any(), anyInt());
        Notification n = notification();
        NotificationDeliveryService service = service();

        service.attempt(n, both);
        service.attempt(n, both);

        assertEquals(DeliveryStatus.SENT, n.getDeliveryStatus());
        assertEquals(2, n.getDeliveryAttempts());
        assertNull(n.getLastError());
        assertNull(n.getNextAttemptAt());
    }

    @Test
    void emailIsUsedWhenThereIsNoPhoneNumber() {
        Notification n = notification();

        service().attempt(n, new Contact("Test", " ", "test@example.com"));

        assertEquals("EMAIL", n.getDeliveryChannel());
        verify(sender).send("EMAIL", "test@example.com", "Booking update", "Your booking #5 was received.", 1);
    }

    @Test
    void withoutAnyContactNothingIsSentAndTheNotificationIsGivenUp() {
        Notification n = notification();

        service().attempt(n, null);

        assertEquals(DeliveryStatus.GAVE_UP, n.getDeliveryStatus());
        assertEquals("NONE", n.getDeliveryChannel());
        assertEquals(0, n.getDeliveryAttempts());
        assertTrue(n.getLastError().contains("No phone or email"));
        verifyNoInteractions(sender);
    }
}
