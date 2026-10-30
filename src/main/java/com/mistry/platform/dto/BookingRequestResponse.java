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
        String customerName,
        Integer refundPercent) {

    public BookingRequestResponse(Long id, Long customerId, String serviceName, String status,
                                  LocalDateTime appointmentTime, LocalDateTime suggestedTime,
                                  LocalDateTime createdAt) {
        this(id, customerId, serviceName, status, appointmentTime, suggestedTime, createdAt, null, null);
    }

    public BookingRequestResponse(Long id, Long customerId, String serviceName, String status,
                                  LocalDateTime appointmentTime, LocalDateTime suggestedTime,
                                  LocalDateTime createdAt, String customerName) {
        this(id, customerId, serviceName, status, appointmentTime, suggestedTime, createdAt, customerName, null);
    }
}
