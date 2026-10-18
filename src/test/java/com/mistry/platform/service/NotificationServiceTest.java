package com.mistry.platform.service;

import com.mistry.platform.entity.DeliveryStatus;
import com.mistry.platform.entity.Notification;
import com.mistry.platform.entity.NotificationType;
import com.mistry.platform.entity.RecipientType;
import com.mistry.platform.exception.NotificationException;
import com.mistry.platform.repository.NotificationRepository;
import com.mistry.platform.service.RecipientDirectory.Contact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;
    @Mock
    private RecipientDirectory directory;
    @Mock
    private NotificationDeliveryService delivery;

    @InjectMocks
    private NotificationService service;

    private final NotificationRecipient customer = new NotificationRecipient(RecipientType.CUSTOMER, 2L);

    private Notification notification(boolean seen) {
        Notification n = new Notification(RecipientType.CUSTOMER, 2L, NotificationType.BOOKING_STATUS,
                "Booking update", "Your booking #5 was received.", "BOOKING", 5L);
        n.setId(5L);
        n.setSeen(seen);
        return n;
    }

    @Test
    void createSavesTheNotificationAfterTryingToDeliverIt() {
        Contact contact = new Contact("Test", "01711111111", "test@example.com");
        when(directory.contactOf(RecipientType.CUSTOMER, 2L)).thenReturn(Optional.of(contact));
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(RecipientType.CUSTOMER, 2L, NotificationType.BOOKING_STATUS,
                "Booking update", "Your booking #5 was received.", "BOOKING", 5L);

        verify(delivery).attempt(any(Notification.class), eq(contact));
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());
        Notification saved = captor.getValue();
        assertEquals(RecipientType.CUSTOMER, saved.getRecipientType());
        assertEquals(2L, saved.getRecipientId());
        assertEquals("BOOKING", saved.getReferenceType());
        assertEquals(5L, saved.getReferenceId());
        assertFalse(saved.isSeen());
    }

    @Test
    void createStillSavesWhenTheRecipientHasNoContactDetails() {
        when(directory.contactOf(RecipientType.CUSTOMER, 2L)).thenReturn(Optional.empty());
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(RecipientType.CUSTOMER, 2L, NotificationType.BOOKING_STATUS,
                "Booking update", "Your booking #5 was received.", "BOOKING", 5L);

        verify(delivery).attempt(any(Notification.class), isNull());
        verify(repository).save(any(Notification.class));
    }

    @Test
    void listCanAskForUnreadNotificationsOnly() {
        service.list(customer, true);
        service.list(customer, false);

        verify(repository).findTop100ByRecipientTypeAndRecipientIdAndSeenFalseOrderByCreatedAtDescIdDesc(
                RecipientType.CUSTOMER, 2L);
        verify(repository).findTop100ByRecipientTypeAndRecipientIdOrderByCreatedAtDescIdDesc(
                RecipientType.CUSTOMER, 2L);
    }

    @Test
    void markReadRecordsWhenTheNotificationWasRead() {
        Notification n = notification(false);
        when(repository.findByIdAndRecipientTypeAndRecipientId(5L, RecipientType.CUSTOMER, 2L))
                .thenReturn(Optional.of(n));

        Notification result = service.markRead(customer, 5L);

        assertTrue(result.isSeen());
        assertNotNull(result.getReadAt());
        verify(repository).save(n);
    }

    @Test
    void markReadOnAnAlreadyReadNotificationChangesNothing() {
        Notification n = notification(true);
        when(repository.findByIdAndRecipientTypeAndRecipientId(5L, RecipientType.CUSTOMER, 2L))
                .thenReturn(Optional.of(n));

        service.markRead(customer, 5L);

        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void anotherUsersNotificationLooksLikeItDoesNotExist() {
        when(repository.findByIdAndRecipientTypeAndRecipientId(5L, RecipientType.CUSTOMER, 2L))
                .thenReturn(Optional.empty());

        NotificationException e = assertThrows(NotificationException.class, () -> service.markRead(customer, 5L));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    void markAllReadMarksEveryUnreadNotification() {
        Notification first = notification(false);
        Notification second = notification(false);
        when(repository.findByRecipientTypeAndRecipientIdAndSeenFalse(RecipientType.CUSTOMER, 2L))
                .thenReturn(List.of(first, second));

        int marked = service.markAllRead(customer);

        assertEquals(2, marked);
        assertTrue(first.isSeen());
        assertTrue(second.isSeen());
        verify(repository).saveAll(anyList());
    }

    @Test
    void retryDueTriesEveryNotificationThatIsDueAndSavesThem() {
        Notification first = notification(false);
        first.setDeliveryStatus(DeliveryStatus.FAILED);
        Notification second = notification(false);
        second.setDeliveryStatus(DeliveryStatus.FAILED);
        when(repository.findByDeliveryStatusAndNextAttemptAtLessThanEqual(
                eq(DeliveryStatus.FAILED), any(LocalDateTime.class))).thenReturn(List.of(first, second));
        when(directory.contactOf(RecipientType.CUSTOMER, 2L))
                .thenReturn(Optional.of(new Contact("Test", "01711111111", null)));

        int retried = service.retryDue();

        assertEquals(2, retried);
        verify(delivery, times(2)).attempt(any(Notification.class), any(Contact.class));
        verify(repository).saveAll(anyList());
    }

    @Test
    void retryDueWithNothingDoesNotTouchTheSender() {
        when(repository.findByDeliveryStatusAndNextAttemptAtLessThanEqual(
                eq(DeliveryStatus.FAILED), any(LocalDateTime.class))).thenReturn(List.of());

        assertEquals(0, service.retryDue());

        verify(delivery, never()).attempt(any(Notification.class), any());
    }
}
