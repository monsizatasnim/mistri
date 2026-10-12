package com.mistry.platform.dto;

import com.mistry.platform.entity.ResolutionDecision;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ResolveComplaintRequest {

    @NotNull(message = "Decision is required")
    private ResolutionDecision decision;

    @NotBlank(message = "Resolution note is required")
    @Size(max = 2000, message = "Resolution note must be at most 2000 characters")
    private String note;

    @DecimalMin(value = "0.01", message = "Refund amount must be greater than zero")
    private BigDecimal refundAmount;
}