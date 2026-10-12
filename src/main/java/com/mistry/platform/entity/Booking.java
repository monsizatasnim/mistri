package com.mistry.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider_id", "appointment_time"}))
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;

    @Column(nullable = false)
    private String status;

    public Booking() {
    }

    public Booking(Long customerId, Long providerId, LocalDateTime appointmentTime, String status) {
        this.customerId = customerId;
        this.providerId = providerId;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    public Long getBookingId() { return bookingId; }
    public Long getCustomerId() { return customerId; }
    public Long getProviderId() { return providerId; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public String getStatus() { return status; }
}