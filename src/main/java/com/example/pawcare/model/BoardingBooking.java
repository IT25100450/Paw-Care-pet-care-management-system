package com.example.pawcare.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Boarding_Booking")
public class BoardingBooking {

    @Id
    @Column(name = "Booking_ID")
    private Integer bookingId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "Booking_ID")
    private Booking booking;

    @Column(name = "CheckoutDate")
    private LocalDate checkoutDate;

    @Column(name = "Kennel_No", length = 20)
    private String kennelNo;

    public BoardingBooking() {}

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public LocalDate getCheckoutDate() { return checkoutDate; }
    public void setCheckoutDate(LocalDate checkoutDate) { this.checkoutDate = checkoutDate; }

    public String getKennelNo() { return kennelNo; }
    public void setKennelNo(String kennelNo) { this.kennelNo = kennelNo; }
}
