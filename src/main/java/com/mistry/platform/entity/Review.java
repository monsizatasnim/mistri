package com.mistry.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    // one review per completed booking
    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(nullable = false)
    private int rating;

    @Column(length = 1000)
    private String comment;

    public Review() {
    }

    public Review(Long providerId, Long bookingId, String customerName, int rating, String comment) {
        this.providerId = providerId;
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public Long getProviderId() { return providerId; }
    public Long getBookingId() { return bookingId; }
    public String getCustomerName() { return customerName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
}