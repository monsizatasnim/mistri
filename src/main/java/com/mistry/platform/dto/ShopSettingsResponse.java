package com.mistry.platform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class ShopSettingsResponse {

    private Long shopOwnerId;
    private String shopName;
    private String location;
    private String contactInfo;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime opensAt;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime closesAt;

    private boolean acceptingOnlineOrders;
    private BigDecimal providerDiscountPercent;
}
