package com.mistry.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AddReferenceRequest {

    @NotNull(message = "Provider id is required")
    private Long providerId;

    @NotBlank(message = "Reference name cannot be empty.")
    private String referenceName;

    @NotBlank(message = "Contact info cannot be empty.")
    private String contactInfo;

    private String description;

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getReferenceName() { return referenceName; }
    public void setReferenceName(String referenceName) { this.referenceName = referenceName; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}