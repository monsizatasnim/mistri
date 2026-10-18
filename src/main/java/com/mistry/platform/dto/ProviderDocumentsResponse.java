package com.mistry.platform.dto;

public class ProviderDocumentsResponse {
    private Long providerId;
    private String nidDocumentUrl;
    private String tradeLicenseDocumentUrl;
    private String experienceCertificateUrl;

    public ProviderDocumentsResponse(Long providerId, String nid, String trade, String exp) {
        this.providerId = providerId;
        this.nidDocumentUrl = nid;
        this.tradeLicenseDocumentUrl = trade;
        this.experienceCertificateUrl = exp;
    }

    public Long getProviderId() { return providerId; }
    public String getNidDocumentUrl() { return nidDocumentUrl; }
    public String getTradeLicenseDocumentUrl() { return tradeLicenseDocumentUrl; }
    public String getExperienceCertificateUrl() { return experienceCertificateUrl; }
}