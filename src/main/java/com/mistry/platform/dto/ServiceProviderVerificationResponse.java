package com.mistry.platform.dto;

import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.VerificationStatus;

import java.time.LocalDateTime;

public class ServiceProviderVerificationResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String nidNumber;
    private String nidDocumentPath;
    private String tradeLicenseNumber;
    private String tradeLicenseDocumentPath;
    private String experienceCertificatePath;
    private VerificationStatus verificationStatus;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private String serviceCategory;
    private String serviceDescription;
    private Double priceMin;
    private Double priceMax;
    private String priceNote;

    public ServiceProviderVerificationResponse(ServiceProvider provider) {
        this.id = provider.getId();
        this.fullName = provider.getFullName();
        this.email = provider.getEmail();
        this.phone = provider.getPhone();
        this.nidNumber = provider.getNidNumber();
        this.nidDocumentPath = provider.getNidDocumentPath();
        this.tradeLicenseNumber = provider.getTradeLicenseNumber();
        this.tradeLicenseDocumentPath = provider.getTradeLicenseDocumentPath();
        this.experienceCertificatePath = provider.getExperienceCertificatePath();
        this.verificationStatus = provider.getVerificationStatus();
        this.rejectionReason = provider.getRejectionReason();
        this.createdAt = provider.getCreatedAt();
        this.serviceCategory = provider.getServiceCategory();
        this.serviceDescription = provider.getServiceDescription();
        this.priceMin = provider.getPriceMin();
        this.priceMax = provider.getPriceMax();
        this.priceNote = provider.getPriceNote();
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getNidNumber() { return nidNumber; }
    public String getNidDocumentPath() { return nidDocumentPath; }
    public String getTradeLicenseNumber() { return tradeLicenseNumber; }
    public String getTradeLicenseDocumentPath() { return tradeLicenseDocumentPath; }
    public String getExperienceCertificatePath() { return experienceCertificatePath; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getServiceCategory() { return serviceCategory; }
    public String getServiceDescription() { return serviceDescription; }
    public Double getPriceMin() { return priceMin; }
    public Double getPriceMax() { return priceMax; }
    public String getPriceNote() { return priceNote; }
}