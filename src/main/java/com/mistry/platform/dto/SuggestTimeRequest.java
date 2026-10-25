package com.mistry.platform.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SuggestTimeRequest(
        @NotNull(message = "Please choose a new date and time")
        LocalDateTime suggestedTime) {
}
