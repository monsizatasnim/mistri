package com.mistry.platform.dto;

import com.mistry.platform.entity.BookingEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BookingInfoResponse {
    private Long bookingId;
    private Long customerId;
    private Long providerId;
    private LocalDateTime appointmentTime;
    private String status;

    public static BookingInfoResponse from(BookingEntity b) {
        return new BookingInfoResponse(
                b.getId(),
                b.getCustomerId(),
                b.getProviderId(),
                b.getAppointmentTime(),
                b.getStatus());
    }
}
