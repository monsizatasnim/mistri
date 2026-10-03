package com.mistry.platform.dto;

import com.mistry.platform.entity.AccountStatus;
import com.mistry.platform.entity.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShopOwnerProfileResponse {
    private Long id;
    private String ownerName;
    private String email;
    private String phone;
    private String shopName;
    private String location;
    private String contactInfo;
    private VerificationStatus verificationStatus;
    private AccountStatus accountStatus;
}
