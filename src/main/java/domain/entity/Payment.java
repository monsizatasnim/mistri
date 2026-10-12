package domain.entity;

import java.time.LocalDateTime;

public class Payment {
    private String paymentId;
    private String referenceId;
    private double amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime timestamp;

    public Payment(String paymentId, String referenceId, double amount, String paymentMethod, String status) {
        this.paymentId = paymentId;
        this.referenceId = referenceId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public String getPaymentId() { return paymentId; }
    public String getReferenceId() { return referenceId; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setStatus(String status) {
        this.status = status;
    }
}