package com.mistry.platform.dto;

import com.mistry.platform.entity.AccountStatus;

import java.time.LocalDateTime;

public class CustomerAccountResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String defaultLocation;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;

    public CustomerAccountResponse(Long id, String fullName, String email, String phone,
                                   String defaultLocation, AccountStatus accountStatus,
                                   LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.defaultLocation = defaultLocation;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDefaultLocation() { return defaultLocation; }
    public AccountStatus getAccountStatus() { return accountStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}