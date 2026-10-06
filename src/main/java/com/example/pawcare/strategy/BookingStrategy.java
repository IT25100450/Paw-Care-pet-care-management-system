package com.example.pawcare.strategy;

import com.example.pawcare.model.Booking;
import com.example.pawcare.model.Pet;
import com.example.pawcare.model.PetOwner;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Strategy interface for creating different PawCare booking types.
 *
 * Each concrete strategy contains the service-specific booking behaviour,
 * while the booking context remains independent of those details.
 */
public interface BookingStrategy {

    /**
     * Creates the service-specific booking information.
     *
     * @param owner       validated pet owner
     * @param pet         validated pet
     * @param bookingTime requested booking date/time
     * @param body        original request data for service-specific fields
     * @return the common Booking entity that was created
     */
    Booking createBooking(PetOwner owner, Pet pet, LocalDateTime bookingTime,
                          Map<String, Object> body);
}
