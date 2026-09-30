package com.mistry.platform.dto;

public class AdminLoginResponse {

    private String token;
    private Long adminId;
    private String fullName;
    private String message;

    public AdminLoginResponse() {
    }

    public AdminLoginResponse(String token, Long adminId, String fullName, String message) {
        this.token = token;
        this.adminId = adminId;
        this.fullName = fullName;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}