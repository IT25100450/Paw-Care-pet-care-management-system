package com.example.pawcare.strategy;

import com.example.pawcare.model.Booking;
import com.example.pawcare.model.GroomingBooking;
import com.example.pawcare.model.Pet;
import com.example.pawcare.model.PetOwner;
import com.example.pawcare.repository.BookingRepository;
import com.example.pawcare.repository.GroomingBookingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Concrete Strategy for PawCare grooming bookings.
 */
@Component("groomingBookingStrategy")
public class GroomingBookingStrategy implements BookingStrategy {

    private final BookingRepository bookingRepository;
    private final GroomingBookingRepository groomingBookingRepository;

    public GroomingBookingStrategy(BookingRepository bookingRepository,
                                   GroomingBookingRepository groomingBookingRepository) {
        this.bookingRepository = bookingRepository;
        this.groomingBookingRepository = groomingBookingRepository;
    }

    @Override
    @Transactional
    public Booking createBooking(PetOwner owner, Pet pet, LocalDateTime bookingTime,
                                 Map<String, Object> body) {

        Booking booking = new Booking();
        booking.setOwner(owner);
        booking.setPet(pet);
        booking.setBookingDateTime(bookingTime);
        booking.setStatus("pending");
        Booking saved = bookingRepository.save(booking);

        GroomingBooking groomingBooking = new GroomingBooking();
        groomingBooking.setBooking(saved);

        String type = (String) body.get("type");
        groomingBooking.setType(type != null && !type.isBlank()
                ? type
                : "Full Grooming");
        groomingBooking.setStatus("pending");

        groomingBookingRepository.save(groomingBooking);
        return saved;
    }
}
