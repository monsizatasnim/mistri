package com.mistry.platform.dto;

public class AdminRegisterResponse {

    private Long adminId;
    private String message;

    public AdminRegisterResponse() {
    }

    public AdminRegisterResponse(Long adminId, String message) {
        this.adminId = adminId;
        this.message = message;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
