package com.mistry.platform.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CustomerBookingRequest(
        @NotNull(message = "Please choose a provider") Long providerId,
        String serviceName,
        @NotNull(message = "Please choose a date and time") LocalDateTime appointmentTime) {
}
