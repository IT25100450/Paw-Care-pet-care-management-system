package com.example.pawcare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Booking_ID")
    private Integer bookingId;

    @ManyToOne
    @JoinColumn(name = "Owner_ID", nullable = false)
    private PetOwner owner;

    @ManyToOne
    @JoinColumn(name = "Pet_ID", nullable = false)
    private Pet pet;

    @Column(name = "Booking_DateTime", nullable = false)
    private LocalDateTime bookingDateTime;

    @Column(name = "Status", length = 20)
    private String status = "pending";

    public Booking() {}

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public PetOwner getOwner() { return owner; }
    public void setOwner(PetOwner owner) { this.owner = owner; }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }

    public LocalDateTime getBookingDateTime() { return bookingDateTime; }
    public void setBookingDateTime(LocalDateTime bookingDateTime) { this.bookingDateTime = bookingDateTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
