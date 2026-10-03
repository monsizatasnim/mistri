package com.mistry.platform.dto;

public class ServiceProviderUpdateResponse {
    private Long providerId;
    private String message;

    public ServiceProviderUpdateResponse(Long providerId, String message) {
        this.providerId = providerId;
        this.message = message;
    }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}