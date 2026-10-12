package com.mistry.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "provider_references")
public class Reference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "reference_name", nullable = false)
    private String referenceName;

    @Column(name = "contact_info", nullable = false)
    private String contactInfo;

    @Column(length = 1000)
    private String description;

    public Reference() {
    }

    public Reference(Long providerId, String referenceName, String contactInfo, String description) {
        this.providerId = providerId;
        this.referenceName = referenceName;
        this.contactInfo = contactInfo;
        this.description = description;
    }

    public Long getId() { return id; }
    public Long getProviderId() { return providerId; }
    public String getReferenceName() { return referenceName; }
    public String getContactInfo() { return contactInfo; }
    public String getDescription() { return description; }
}