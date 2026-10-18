package com.mistry.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeclineOrderRequest(
        @NotBlank(message = "Please give a reason for declining")
        @Size(max = 500, message = "Reason can be at most 500 characters")
        String reason) {
}
