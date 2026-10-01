package com.mistry.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_providers")
public class ServiceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String phone;

    @Column(unique = true)
    private String email;

    private String password;

    private String nidNumber;
    private String nidDocumentPath;

    private String tradeLicenseNumber;
    private String tradeLicenseDocumentPath;

    private String experienceCertificatePath;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    private String rejectionReason;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNidNumber() { return nidNumber; }
    public void setNidNumber(String nidNumber) { this.nidNumber = nidNumber; }

    public String getNidDocumentPath() { return nidDocumentPath; }
    public void setNidDocumentPath(String nidDocumentPath) { this.nidDocumentPath = nidDocumentPath; }

    public String getTradeLicenseNumber() { return tradeLicenseNumber; }
    public void setTradeLicenseNumber(String tradeLicenseNumber) { this.tradeLicenseNumber = tradeLicenseNumber; }

    public String getTradeLicenseDocumentPath() { return tradeLicenseDocumentPath; }
    public void setTradeLicenseDocumentPath(String tradeLicenseDocumentPath) { this.tradeLicenseDocumentPath = tradeLicenseDocumentPath; }

    public String getExperienceCertificatePath() { return experienceCertificatePath; }
    public void setExperienceCertificatePath(String experienceCertificatePath) { this.experienceCertificatePath = experienceCertificatePath; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}