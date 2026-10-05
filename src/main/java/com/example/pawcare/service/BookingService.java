package com.example.pawcare.service;

import com.example.pawcare.model.Booking;
import com.example.pawcare.model.Pet;
import com.example.pawcare.model.PetOwner;
import com.example.pawcare.repository.PetOwnerRepository;
import com.example.pawcare.repository.PetRepository;
import com.example.pawcare.strategy.BookingStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Context for the Strategy Pattern.
 *
 * The context selects the requested booking strategy and delegates the
 * service-specific booking operation to it.
 */
@Service
public class BookingService {

    private final PetOwnerRepository petOwnerRepository;
    private final PetRepository petRepository;
    private final Map<String, BookingStrategy> strategies;

    public BookingService(PetOwnerRepository petOwnerRepository,
                          PetRepository petRepository,
                          Map<String, BookingStrategy> strategies) {
        this.petOwnerRepository = petOwnerRepository;
        this.petRepository = petRepository;
        this.strategies = strategies;
    }

    /**
     * Selects a strategy at runtime and delegates booking creation to it.
     */
    @Transactional
    public Booking createBooking(String strategyName, Map<String, Object> body) {
        Integer ownerId = getRequiredInteger(body, "ownerId");
        Integer petId = getRequiredInteger(body, "petId");
        String dateTime = getRequiredString(body, "bookingDateTime");

        PetOwner owner = petOwnerRepository.findById(ownerId).orElse(null);
        if (owner == null) {
            throw new IllegalArgumentException("Owner not found: " + ownerId);
        }

        Pet pet = petRepository.findById(petId).orElse(null);
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found: " + petId);
        }

        if (pet.getOwner() == null || pet.getOwner().getOwnerId() == null
                || !pet.getOwner().getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Pet does not belong to this owner");
        }

        BookingStrategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported booking strategy: " + strategyName);
        }

        return strategy.createBooking(owner, pet, LocalDateTime.parse(dateTime), body);
    }

    private Integer getRequiredInteger(Map<String, Object> body, String field) {
        Object value = body.get(field);
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException(field + " is required");
        }
        return ((Number) value).intValue();
    }

    private String getRequiredString(Map<String, Object> body, String field) {
        Object value = body.get(field);
        if (!(value instanceof String) || ((String) value).isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return (String) value;
    }
}
