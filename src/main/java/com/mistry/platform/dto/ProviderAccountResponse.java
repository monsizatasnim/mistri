package com.mistry.platform.dto;

import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.VerificationStatus;

import java.time.LocalDateTime;

public class ProviderAccountResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String nidNumber;
    private String tradeLicenseNumber;
    private VerificationStatus verificationStatus;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;

    public ProviderAccountResponse(Long id, String fullName, String email, String phone,
                                   String nidNumber, String tradeLicenseNumber,
                                   VerificationStatus verificationStatus,
                                   AccountStatus accountStatus, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.nidNumber = nidNumber;
        this.tradeLicenseNumber = tradeLicenseNumber;
        this.verificationStatus = verificationStatus;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getNidNumber() { return nidNumber; }
    public String getTradeLicenseNumber() { return tradeLicenseNumber; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public AccountStatus getAccountStatus() { return accountStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}