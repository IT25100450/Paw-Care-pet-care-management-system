package com.example.pawcare.strategy;

import com.example.pawcare.model.BoardingBooking;
import com.example.pawcare.model.Booking;
import com.example.pawcare.model.Pet;
import com.example.pawcare.model.PetOwner;
import com.example.pawcare.repository.BoardingBookingRepository;
import com.example.pawcare.repository.BookingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Concrete Strategy for PawCare boarding bookings.
 */
@Component("boardingBookingStrategy")
public class BoardingBookingStrategy implements BookingStrategy {

    private final BookingRepository bookingRepository;
    private final BoardingBookingRepository boardingBookingRepository;

    public BoardingBookingStrategy(BookingRepository bookingRepository,
                                   BoardingBookingRepository boardingBookingRepository) {
        this.bookingRepository = bookingRepository;
        this.boardingBookingRepository = boardingBookingRepository;
    }

    @Override
    @Transactional
    public Booking createBooking(PetOwner owner, Pet pet, LocalDateTime bookingTime,
                                 Map<String, Object> body) {

        Integer petId = pet.getPetId();
        LocalDate today = LocalDate.now();

        // Preserve the existing PawCare rule: a pet cannot have another
        // pending/confirmed boarding that has not been checked out.
        Optional<BoardingBooking> open = boardingBookingRepository.findAllWithDetails().stream()
                .filter(x -> x.getBooking().getPet().getPetId().equals(petId))
                .filter(x -> {
                    String status = x.getBooking().getStatus() == null
                            ? "pending"
                            : x.getBooking().getStatus().toLowerCase();
                    return status.equals("pending") || status.equals("confirmed");
                })
                .filter(x -> x.getCheckoutDate() == null || !x.getCheckoutDate().isBefore(today))
                .findFirst();

        if (open.isPresent()) {
            BoardingBooking existing = open.get();
            throw new BookingConflictException(
                    pet.getName() + " already has an active boarding booking (#"
                            + existing.getBookingId()
                            + ", status: " + existing.getBooking().getStatus()
                            + (existing.getCheckoutDate() != null
                            ? ", check-out " + existing.getCheckoutDate()
                            : "")
                            + "). It must be completed or cancelled before booking again."
            );
        }

        Booking booking = new Booking();
        booking.setOwner(owner);
        booking.setPet(pet);
        booking.setBookingDateTime(bookingTime);
        booking.setStatus("pending");
        Booking saved = bookingRepository.save(booking);

        BoardingBooking boardingBooking = new BoardingBooking();
        boardingBooking.setBooking(saved);

        String checkoutDate = (String) body.get("checkoutDate");
        if (checkoutDate != null && !checkoutDate.isBlank()) {
            boardingBooking.setCheckoutDate(LocalDate.parse(checkoutDate));
        }

        // Kennel is assigned later by the Boarding Services Manager.
        boardingBooking.setKennelNo(null);
        boardingBookingRepository.save(boardingBooking);

        return saved;
    }
}
