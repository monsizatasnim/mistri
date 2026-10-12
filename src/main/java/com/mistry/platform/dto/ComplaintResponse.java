package com.mistry.platform.dto;

import com.mistry.platform.entity.ComplaintCategory;
import com.mistry.platform.entity.ComplaintStatus;
import com.mistry.platform.entity.ResolutionDecision;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ComplaintResponse {
    private Long id;
    private Long customerId;
    private String bookingId;
    private ComplaintCategory category;
    private String description;
    private ComplaintStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ResolutionDecision resolutionDecision;
    private String resolutionNote;
    private BigDecimal refundAmount;
    private LocalDateTime resolvedAt;
}