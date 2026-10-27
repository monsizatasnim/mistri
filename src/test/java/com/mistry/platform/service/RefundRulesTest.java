package com.mistry.platform.service;

import com.mistry.platform.entity.BookingEntity;
import com.mistry.platform.repository.BookingRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefundRulesTest {

    private final BookingRepository repo = mock(BookingRepository.class);
    private final BookingService service = new BookingService(repo);

    private void existingBooking(String status) {
        BookingEntity b = new BookingEntity(1L, "Plumbing", status);
        when(repo.findById(1L)).thenReturn(Optional.of(b));
        when(repo.save(any(BookingEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void cancelBeforeProviderResponseGivesFullRefund() {
        existingBooking("PENDING");
        assertEquals("CANCELLED_FULL_REFUND", service.cancelBooking(1L).getStatus());
    }

    @Test
    void cancelAfterProviderConfirmedHasNoFullRefund() {
        existingBooking("CONFIRMED");
        assertEquals("CANCELLED", service.cancelBooking(1L).getStatus());
    }

    @Test
    void cancelAfterProviderAcceptedHasNoFullRefund() {
        existingBooking("ACCEPTED");
        assertEquals("CANCELLED", service.cancelBooking(1L).getStatus());
    }

    @Test
    void completedBookingCannotBeCancelled() {
        existingBooking("COMPLETED");
        assertThrows(RuntimeException.class, () -> service.cancelBooking(1L));
    }

    @Test
    void unknownBookingCannotBeCancelled() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.cancelBooking(99L));
    }

    @Test
    void rescheduleNeedsProviderApproval() {
        existingBooking("CONFIRMED");
        assertEquals("RESCHEDULE_REQUESTED", service.rescheduleBooking(1L).getStatus());
    }

    @Test
    void onlyConfirmedBookingCanStart() {
        existingBooking("PENDING");
        assertThrows(RuntimeException.class, () -> service.startBooking(1L));
    }

    @Test
    void confirmedBookingStartsThenCompletes() {
        existingBooking("CONFIRMED");
        assertEquals("IN_PROGRESS", service.startBooking(1L).getStatus());
        assertEquals("COMPLETED", service.completeBooking(1L).getStatus());
    }

    @Test
    void cancelledBookingCannotStart() {
        existingBooking("CANCELLED");
        assertThrows(RuntimeException.class, () -> service.startBooking(1L));
    }
}