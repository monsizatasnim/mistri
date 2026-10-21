package com.mistry.platform.dto;

public class PublicProviderProfileResponse {
    private Long id;
    private String fullName;
    private String serviceCategory;
    private String serviceDescription;
    private String serviceLocation;
    private Double priceMin;
    private Double priceMax;
    private String priceNote;
    private boolean verified;

    public PublicProviderProfileResponse(Long id, String fullName, String serviceCategory,
                                         String serviceDescription, String serviceLocation,
                                         Double priceMin, Double priceMax, String priceNote,
                                         boolean verified) {
        this.id = id;
        this.fullName = fullName;
        this.serviceCategory = serviceCategory;
        this.serviceDescription = serviceDescription;
        this.serviceLocation = serviceLocation;
        this.priceMin = priceMin;
        this.priceMax = priceMax;
        this.priceNote = priceNote;
        this.verified = verified;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getServiceCategory() { return serviceCategory; }
    public String getServiceDescription() { return serviceDescription; }
    public String getServiceLocation() { return serviceLocation; }
    public Double getPriceMin() { return priceMin; }
    public Double getPriceMax() { return priceMax; }
    public String getPriceNote() { return priceNote; }
    public boolean isVerified() { return verified; }
}