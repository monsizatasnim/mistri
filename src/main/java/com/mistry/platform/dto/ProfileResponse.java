package com.mistry.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String defaultLocation;
    private String profilePictureUrl;
}
