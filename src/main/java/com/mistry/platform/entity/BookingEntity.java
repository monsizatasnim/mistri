package com.mistry.platform.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_entities")
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "provider_id")
    private Long providerId;

    @Column(name = "appointment_time")
    private LocalDateTime appointmentTime;

    @Column(name = "suggested_time")
    private LocalDateTime suggestedTime;

    public BookingEntity() {
    }

    public BookingEntity(Long customerId, String serviceName, String status) {
        this.customerId = customerId;
        this.serviceName = serviceName;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalDateTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public LocalDateTime getSuggestedTime() { return suggestedTime; }
    public void setSuggestedTime(LocalDateTime suggestedTime) { this.suggestedTime = suggestedTime; }
}

