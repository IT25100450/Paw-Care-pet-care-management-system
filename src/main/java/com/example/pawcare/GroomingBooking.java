package com.example.pawcare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Grooming_Booking")
public class GroomingBooking {

    @Id
    @Column(name = "Booking_ID")
    private Integer bookingId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "Booking_ID")
    private Booking booking;

    @Column(name = "Type", length = 25)
    private String type;

    @ManyToOne
    @JoinColumn(name = "Groomer_ID", nullable = true)
    private Groomer groomer;

    @Column(name = "Status", length = 20)
    private String status = "pending";

    public GroomingBooking() {}

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Groomer getGroomer() { return groomer; }
    public void setGroomer(Groomer groomer) { this.groomer = groomer; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
