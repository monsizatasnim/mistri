package com.mistry.platform.dto;

import java.time.LocalDateTime;

public record BookingRequestResponse(
        Long id,
        Long customerId,
        String serviceName,
        String status,
        LocalDateTime appointmentTime,
        LocalDateTime suggestedTime,
        LocalDateTime createdAt,
        String customerName) {

    public BookingRequestResponse(Long id, Long customerId, String serviceName, String status,
                                  LocalDateTime appointmentTime, LocalDateTime suggestedTime,
                                  LocalDateTime createdAt) {
        this(id, customerId, serviceName, status, appointmentTime, suggestedTime, createdAt, null);
    }
}
