package com.mistry.platform.dto;

public class ServiceProviderLoginResponse {
    private String token;
    private Long providerId;
    private String fullName;

    public ServiceProviderLoginResponse(String token, Long providerId, String fullName) {
        this.token = token;
        this.providerId = providerId;
        this.fullName = fullName;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}