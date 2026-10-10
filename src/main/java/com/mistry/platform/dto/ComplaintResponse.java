package com.mistry.platform.dto;

import com.mistry.platform.entity.ComplaintCategory;
import com.mistry.platform.entity.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
}
