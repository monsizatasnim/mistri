package com.mistry.platform.dto;

import com.mistry.platform.entity.ServiceProvider;
import com.mistry.platform.entity.VerificationStatus;

public class ProviderSearchResponse {

    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String serviceCategory;
    private String serviceDescription;
    private String serviceLocation;
    private Double priceMin;
    private Double priceMax;
    private String priceNote;
    private VerificationStatus verificationStatus;

    public ProviderSearchResponse(ServiceProvider provider) {
        this.id = provider.getId();
        this.fullName = provider.getFullName();
        this.phone = provider.getPhone();
        this.email = provider.getEmail();
        this.serviceCategory = provider.getServiceCategory();
        this.serviceDescription = provider.getServiceDescription();
        this.serviceLocation = provider.getServiceLocation();
        this.priceMin = provider.getPriceMin();
        this.priceMax = provider.getPriceMax();
        this.priceNote = provider.getPriceNote();
        this.verificationStatus = provider.getVerificationStatus();
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getServiceCategory() { return serviceCategory; }
    public String getServiceDescription() { return serviceDescription; }
    public String getServiceLocation() { return serviceLocation; }
    public Double getPriceMin() { return priceMin; }
    public Double getPriceMax() { return priceMax; }
    public String getPriceNote() { return priceNote; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
}
