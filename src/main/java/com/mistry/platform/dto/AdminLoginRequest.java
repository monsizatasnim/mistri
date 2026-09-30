package com.mistry.platform.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminLoginRequest {

    @NotBlank(message = "Email is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    public AdminLoginRequest() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}