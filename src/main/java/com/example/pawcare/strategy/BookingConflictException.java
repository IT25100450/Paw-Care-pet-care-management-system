package com.example.pawcare.strategy;

/**
 * Indicates that a requested booking conflicts with an existing booking.
 */
public class BookingConflictException extends RuntimeException {
    public BookingConflictException(String message) {
        super(message);
    }
}
