package com.mistry.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ComplaintPartiesResponse {
    private String customerName;
    private String customerPhone;
    private String serviceName;
    private Long providerId;
    private String providerName;
    private String providerPhone;
}
