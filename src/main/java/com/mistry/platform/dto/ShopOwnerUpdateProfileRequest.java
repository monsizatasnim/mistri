package com.mistry.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopOwnerUpdateProfileRequest {

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    private String phone;

    @NotBlank(message = "Shop name is required")
    private String shopName;

    @NotBlank(message = "Shop location is required")
    private String location;

    @NotBlank(message = "Contact information is required")
    private String contactInfo;
}
