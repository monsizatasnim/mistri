package com.mistry.platform.service;

import com.mistry.platform.dto.ComplaintResponse;
import com.mistry.platform.dto.ShopOrderResponse;
import com.mistry.platform.dto.VerificationDecisionRequest;
import com.mistry.platform.entity.BookingEntity;
import com.mistry.platform.entity.ComplaintCategory;
import com.mistry.platform.entity.ComplaintStatus;
import com.mistry.platform.entity.RecipientType;
import com.mistry.platform.entity.ResolutionDecision;
import com.mistry.platform.entity.ShopOrderStatus;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusNotificationAspectTest {

    @Mock
    private StatusNotifier notifier;
    @Mock
    private RecipientDirectory directory;

    private StatusNotificationAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new StatusNotificationAspect(notifier, directory);
    }

    private JoinPoint joinPoint(Object... args) {
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(args);
        return joinPoint;
    }

    private BookingEntity booking() {
        BookingEntity booking = new BookingEntity(2L, "General Service", "CONFIRMED");
        booking.setId(5L);
        return booking;
    }

    private ShopOrderResponse order() {
        return new ShopOrderResponse(11L, 3L, "Karim Hardware", true, null, null,
                ShopOrderStatus.PLACED, BigDecimal.TEN, null, List.of());
    }

    @Test
    void aBookingChangeIsPassedOnWithTheCustomerAndTheStatus() {
        aspect.afterBookingChange(booking());

        verify(notifier).bookingChanged(2L, 5L, "CONFIRMED");
    }

    @Test
    void aMissingBookingIsIgnored() {
        aspect.afterBookingChange(null);

        verifyNoInteractions(notifier);
    }

    @Test
    void aDisputeChangeIsPassedOnWithTheDecisionAndTheRefund() {
        ComplaintResponse complaint = new ComplaintResponse(9L, 2L, "1", ComplaintCategory.QUALITY_ISSUE,
                "Work was not finished", ComplaintStatus.RESOLVED, null, null,
                ResolutionDecision.REFUND_ISSUED, "Partial refund agreed", new BigDecimal("250"), null);

        aspect.afterDisputeChange(complaint);

        verify(notifier).disputeChanged(2L, 9L, ComplaintStatus.RESOLVED,
                ResolutionDecision.REFUND_ISSUED, new BigDecimal("250"));
    }

    @Test
    void aProviderApprovalNotifiesTheProviderWithThePathId() {
        aspect.afterProviderApproved(joinPoint(4L));

        verify(notifier).verificationDecided(RecipientType.PROVIDER, 4L, true, null);
    }

    @Test
    void aProviderRejectionCarriesTheReason() {
        VerificationDecisionRequest request = mock(VerificationDecisionRequest.class);
        when(request.getReason()).thenReturn("Blurry NID");

        aspect.afterProviderRejected(joinPoint(6L, request));

        verify(notifier).verificationDecided(RecipientType.PROVIDER, 6L, false, "Blurry NID");
    }

    @Test
    void aShopOwnerApprovalNotifiesTheShopOwner() {
        aspect.afterShopOwnerApproved(joinPoint(3L));

        verify(notifier).verificationDecided(RecipientType.SHOP_OWNER, 3L, true, null);
    }

    @Test
    void aShopOwnerRejectionCarriesTheReason() {
        VerificationDecisionRequest request = mock(VerificationDecisionRequest.class);
        when(request.getReason()).thenReturn("Documents unclear");

        aspect.afterShopOwnerRejected(joinPoint(5L, request));

        verify(notifier).verificationDecided(RecipientType.SHOP_OWNER, 5L, false, "Documents unclear");
    }

    @Test
    void aNewOrderIsPassedOnWithTheProvidersId() {
        when(directory.idOf(RecipientType.PROVIDER, "provider@test.com")).thenReturn(Optional.of(7L));

        aspect.afterOrderPlaced(joinPoint("provider@test.com", null), order());

        verify(notifier).orderPlaced(7L, 3L, 11L, "Karim Hardware", true);
    }

    @Test
    void anOrderFromAnUnknownProviderStillNotifiesTheShop() {
        when(directory.idOf(RecipientType.PROVIDER, "ghost@test.com")).thenReturn(Optional.empty());

        aspect.afterOrderPlaced(joinPoint("ghost@test.com", null), order());

        verify(notifier).orderPlaced(null, 3L, 11L, "Karim Hardware", true);
    }

    @Test
    void markingAnOrderUrgentNotifiesTheShop() {
        aspect.afterOrderMarkedUrgent(order());

        verify(notifier).orderMarkedUrgent(3L, 11L);
    }

    @Test
    void aFailureWhileNotifyingNeverBreaksTheMainAction() {
        doThrow(new RuntimeException("db down")).when(notifier).bookingChanged(any(), any(), any());

        assertDoesNotThrow(() -> aspect.afterBookingChange(booking()));
    }
}
