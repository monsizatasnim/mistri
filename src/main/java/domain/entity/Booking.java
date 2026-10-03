package domain.entity;

import java.time.LocalDateTime;

public class Booking {
    private String bookingId;
    private String customerId;
    private String providerId;
    private LocalDateTime appointmentTime;
    private String status;

    public Booking(String bookingId, String customerId, String providerId, LocalDateTime appointmentTime, String status) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.providerId = providerId;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    public String getBookingId() { return bookingId; }
    public String getCustomerId() { return customerId; }
    public String getProviderId() { return providerId; }
    public LocalDateTime getAppointmentTime() { return appointmentTime; }
    public String getStatus() { return status; }
}