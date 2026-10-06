package com.example.pawcare.controller;

import com.example.pawcare.model.*;
import com.example.pawcare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boarding")
public class BoardingController {

    @Autowired private BoardingBookingRepository boardingBookingRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private PetOwnerRepository petOwnerRepository;
    @Autowired private PetRepository petRepository;

    /* ── GET all boarding bookings ─────────────────────────────── */
    @GetMapping
    public List<Map<String, Object>> getAll() {
        return boardingBookingRepository.findAllWithDetails().stream()
                .map(this::toView).collect(Collectors.toList());
    }

    /* ── POST create boarding booking ─────────────────────────── */
    @PostMapping
    public synchronized ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Integer ownerId = body.get("ownerId") instanceof Integer ? (Integer) body.get("ownerId") : Integer.parseInt(body.get("ownerId").toString());
            Integer petId = body.get("petId") instanceof Integer ? (Integer) body.get("petId") : Integer.parseInt(body.get("petId").toString());
            String dateTime = (String) body.get("bookingDateTime");
            String checkoutDate = (String) body.get("checkoutDate");
            String kennelNo = (String) body.get("kennelNo");

            PetOwner owner = petOwnerRepository.findById(ownerId).orElse(null);
            if (owner == null) return ResponseEntity.badRequest().body(Map.of("error", "Owner not found"));

            Pet pet = petRepository.findById(petId).orElse(null);
            if (pet == null) return ResponseEntity.badRequest().body(Map.of("error", "Pet not found"));

            Booking booking = new Booking();
            booking.setOwner(owner);
            booking.setPet(pet);
            booking.setBookingDateTime(LocalDateTime.parse(dateTime));
            Booking savedBooking = bookingRepository.save(booking);

            BoardingBooking bb = new BoardingBooking();
            bb.setBooking(savedBooking);
            if (checkoutDate != null && !checkoutDate.isBlank())
                bb.setCheckoutDate(java.time.LocalDate.parse(checkoutDate));
            bb.setKennelNo(kennelNo);
            boardingBookingRepository.save(bb);

            return ResponseEntity.ok(toView(bb));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> toView(BoardingBooking bb) {
        Booking b = bb.getBooking();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("bookingId", b.getBookingId());
        m.put("bookingDateTime", b.getBookingDateTime() != null ? b.getBookingDateTime().toString() : null);
        m.put("checkoutDate", bb.getCheckoutDate() != null ? bb.getCheckoutDate().toString() : null);
        m.put("kennelNo", bb.getKennelNo());
        m.put("ownerName", b.getOwner().getFirstName() + " " + b.getOwner().getLastName());
        m.put("ownerEmail", b.getOwner().getEmail());
        m.put("ownerId", b.getOwner().getOwnerId());
        m.put("petName", b.getPet().getName());
        m.put("petId", b.getPet().getPetId());
        return m;
    }
}
