package com.mistry.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "shop_settings")
@Getter
@Setter
@NoArgsConstructor
public class ShopSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long shopOwnerId;

    private LocalTime opensAt;

    private LocalTime closesAt;

    @Column(nullable = false)
    private boolean acceptingOnlineOrders = true;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal providerDiscountPercent = BigDecimal.ZERO;

    private LocalDateTime updatedAt = LocalDateTime.now();
}
