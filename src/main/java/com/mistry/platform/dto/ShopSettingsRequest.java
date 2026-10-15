package com.mistry.platform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
public class ShopSettingsRequest {

    @JsonFormat(pattern = "HH:mm")
    private LocalTime opensAt;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime closesAt;

    @NotNull(message = "Please choose whether online orders are on or off")
    private Boolean acceptingOnlineOrders;

    @DecimalMin(value = "0.00", message = "Discount cannot be below 0")
    @DecimalMax(value = "100.00", message = "Discount cannot be above 100")
    @Digits(integer = 3, fraction = 2, message = "Discount can have at most 2 decimal places")
    private BigDecimal providerDiscountPercent;
}
