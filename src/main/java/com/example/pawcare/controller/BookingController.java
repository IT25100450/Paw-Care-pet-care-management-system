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
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private PetOwnerRepository petOwnerRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private BoardingBookingRepository boardingBookingRepository;
    @Autowired private GroomingBookingRepository groomingBookingRepository;

    /* ── GET /api/bookings ─────────────────────────────────────── */
    @GetMapping
    public List<Map<String, Object>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAllWithDetails();
        Map<Integer, String> groomingTypes = new HashMap<>();
        groomingBookingRepository.findAll().forEach(gb -> groomingTypes.put(gb.getBookingId(), gb.getType()));
        Set<Integer> boardingIds = new HashSet<>();
        boardingBookingRepository.findAll().forEach(bb -> boardingIds.add(bb.getBookingId()));
        return bookings.stream().map(b -> toView(b, groomingTypes, boardingIds)).collect(Collectors.toList());
    }

    /* ── GET /api/bookings/owner/{ownerId} ─────────────────────── */
    @GetMapping("/owner/{ownerId}")
    public List<Map<String, Object>> getByOwner(@PathVariable Integer ownerId) {
        Map<Integer, String> groomingTypes = new HashMap<>();
        groomingBookingRepository.findAll().forEach(gb -> groomingTypes.put(gb.getBookingId(), gb.getType()));
        Set<Integer> boardingIds = new HashSet<>();
        boardingBookingRepository.findAll().forEach(bb -> boardingIds.add(bb.getBookingId()));
        return bookingRepository.findByOwnerOwnerId(ownerId).stream()
                .map(b -> toView(b, groomingTypes, boardingIds)).collect(Collectors.toList());
    }

    /* ── POST /api/bookings/boarding ───────────────────────────── */
    @PostMapping("/boarding")
    public ResponseEntity<?> createBoardingBooking(@RequestBody Map<String, Object> body) {
        try {
            Integer ownerId     = ((Number) body.get("ownerId")).intValue();
            Integer petId       = ((Number) body.get("petId")).intValue();
            String dateTime     = (String) body.get("bookingDateTime");
            String checkoutDate = (String) body.get("checkoutDate");
            // Kennel_No is null — assigned by Boarding Services Manager when confirming
            // String specialCare  = (String) body.get("specialCareInstructions");

            PetOwner owner = petOwnerRepository.findById(ownerId).orElse(null);
            if (owner == null) return ResponseEntity.badRequest().body(Map.of("error", "Owner not found: " + ownerId));
            Pet pet = petRepository.findById(petId).orElse(null);
            if (pet == null) return ResponseEntity.badRequest().body(Map.of("error", "Pet not found: " + petId));
            if (!pet.getOwner().getOwnerId().equals(ownerId))
                return ResponseEntity.badRequest().body(Map.of("error", "Pet does not belong to this owner"));

            // Prevent double-boarding: pet already has a pending/confirmed boarding not yet checked out
            java.time.LocalDate today = java.time.LocalDate.now();
            Optional<BoardingBooking> open = boardingBookingRepository.findAllWithDetails().stream()
                    .filter(x -> x.getBooking().getPet().getPetId().equals(petId))
                    .filter(x -> {
                        String s = x.getBooking().getStatus() == null ? "pending" : x.getBooking().getStatus().toLowerCase();
                        return s.equals("pending") || s.equals("confirmed");
                    })
                    .filter(x -> x.getCheckoutDate() == null || !x.getCheckoutDate().isBefore(today))
                    .findFirst();
            if (open.isPresent()) {
                BoardingBooking ex = open.get();
                return ResponseEntity.status(409).body(Map.of("error",
                        pet.getName() + " already has an active boarding booking (#" + ex.getBookingId()
                        + ", status: " + ex.getBooking().getStatus()
                        + (ex.getCheckoutDate() != null ? ", check-out " + ex.getCheckoutDate() : "")
                        + "). It must be completed or cancelled before booking again."));
            }

            Booking booking = new Booking();
            booking.setOwner(owner);
            booking.setPet(pet);
            booking.setBookingDateTime(LocalDateTime.parse(dateTime));
            booking.setStatus("pending");
            Booking saved = bookingRepository.save(booking);

            BoardingBooking bb = new BoardingBooking();
            bb.setBooking(saved);
            if (checkoutDate != null && !checkoutDate.isBlank())
                bb.setCheckoutDate(java.time.LocalDate.parse(checkoutDate));
            bb.setKennelNo(null); // assigned by manager on confirmation
            boardingBookingRepository.save(bb);

            return ResponseEntity.ok(toView(saved, new HashMap<>(), Set.of(saved.getBookingId())));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    /* ── POST /api/bookings/grooming ───────────────────────────── */
    @PostMapping("/grooming")
    public ResponseEntity<?> createGroomingBooking(@RequestBody Map<String, Object> body) {
        try {
            Integer ownerId = ((Number) body.get("ownerId")).intValue();
            Integer petId   = ((Number) body.get("petId")).intValue();
            String dateTime = (String) body.get("bookingDateTime");
            String type     = (String) body.get("type");

            PetOwner owner = petOwnerRepository.findById(ownerId).orElse(null);
            if (owner == null) return ResponseEntity.badRequest().body(Map.of("error", "Owner not found: " + ownerId));
            Pet pet = petRepository.findById(petId).orElse(null);
            if (pet == null) return ResponseEntity.badRequest().body(Map.of("error", "Pet not found: " + petId));
            if (!pet.getOwner().getOwnerId().equals(ownerId))
                return ResponseEntity.badRequest().body(Map.of("error", "Pet does not belong to this owner"));

            Booking booking = new Booking();
            booking.setOwner(owner);
            booking.setPet(pet);
            booking.setBookingDateTime(LocalDateTime.parse(dateTime));
            booking.setStatus("pending");
            Booking saved = bookingRepository.save(booking);

            GroomingBooking gb = new GroomingBooking();
            gb.setBooking(saved);
            gb.setType(type != null ? type : "Full Grooming");
            groomingBookingRepository.save(gb);

            String resolvedType = type != null ? type : "Full Grooming";
            return ResponseEntity.ok(toView(saved, Map.of(saved.getBookingId(), resolvedType), Collections.emptySet()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    /* ── PATCH /api/bookings/{id} ──────────────────────────────── */
    @PatchMapping("/{id}")
    public ResponseEntity<?> patchStatus(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Booking b = bookingRepository.findById(id).orElse(null);
        if (b == null) return ResponseEntity.notFound().build();

        String newStatus = (String) body.get("status");
        if (newStatus != null) {
            b.setStatus(newStatus);
            bookingRepository.save(b);
        }

        // Also update kennel assignment for boarding if provided
        String kennelNo = (String) body.get("kennelNo");
        if (kennelNo != null) {
            boardingBookingRepository.findById(id).ifPresent(bb -> {
                bb.setKennelNo(kennelNo);
                boardingBookingRepository.save(bb);
            });
        }

        Map<Integer, String> groomingTypes = new HashMap<>();
        groomingBookingRepository.findAll().forEach(gb -> groomingTypes.put(gb.getBookingId(), gb.getType()));
        Set<Integer> boardingIds = new HashSet<>();
        boardingBookingRepository.findAll().forEach(bb -> boardingIds.add(bb.getBookingId()));

        return ResponseEntity.ok(toView(b, groomingTypes, boardingIds));
    }

    /* ── PUT /api/bookings/{id}/boarding  (admin edit) ──────────── */
    @PutMapping("/{id}/boarding")
    public ResponseEntity<?> updateBoarding(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        BoardingBooking bb = boardingBookingRepository.findById(id).orElse(null);
        if (bb == null) return ResponseEntity.notFound().build();
        Booking b = bb.getBooking();
        try {
            String checkin  = (String) body.get("checkinDate");
            String checkout = (String) body.get("checkoutDate");
            String kennelNo = (String) body.get("kennelNo");
            String status   = (String) body.get("status");

            java.time.LocalDate in  = (checkin  != null && !checkin.isBlank())  ? java.time.LocalDate.parse(checkin)
                    : (b.getBookingDateTime() != null ? b.getBookingDateTime().toLocalDate() : null);
            java.time.LocalDate out = (checkout != null && !checkout.isBlank()) ? java.time.LocalDate.parse(checkout) : bb.getCheckoutDate();
            if (in != null && out != null && !out.isAfter(in))
                return ResponseEntity.badRequest().body(Map.of("error", "Check-out date must be after check-in date."));

            if (in != null) {
                java.time.LocalTime t = b.getBookingDateTime() != null ? b.getBookingDateTime().toLocalTime() : java.time.LocalTime.of(8, 0);
                b.setBookingDateTime(in.atTime(t));
            }
            bb.setCheckoutDate(out);
            if (kennelNo != null) bb.setKennelNo(kennelNo.isBlank() ? null : kennelNo.trim());
            if (status != null && !status.isBlank()) b.setStatus(status.toLowerCase());

            bookingRepository.save(b);
            boardingBookingRepository.save(bb);
            return ResponseEntity.ok(toView(b, new HashMap<>(), Set.of(b.getBookingId())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid data"));
        }
    }

    /* ── DELETE /api/bookings/{id} ─────────────────────────────── */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Integer id) {
        if (!bookingRepository.existsById(id)) return ResponseEntity.notFound().build();
        // Sub-tables (Grooming_Booking, Boarding_Booking) must be deleted first
        groomingBookingRepository.findById(id).ifPresent(groomingBookingRepository::delete);
        boardingBookingRepository.findById(id).ifPresent(boardingBookingRepository::delete);
        bookingRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }

    /* ── Helper ─────────────────────────────────────────────────── */
    private Map<String, Object> toView(Booking b, Map<Integer, String> groomingTypes, Set<Integer> boardingIds) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("bookingId",       b.getBookingId());
        m.put("bookingDateTime", b.getBookingDateTime() != null ? b.getBookingDateTime().toString() : null);
        m.put("ownerName",       b.getOwner().getFirstName() + " " + b.getOwner().getLastName());
        m.put("ownerEmail",      b.getOwner().getEmail());
        m.put("ownerId",         b.getOwner().getOwnerId());
        m.put("petName",         b.getPet().getName());
        m.put("petId",           b.getPet().getPetId());
        m.put("status",          b.getStatus() != null ? b.getStatus() : "pending");

        String service;
        if (groomingTypes.containsKey(b.getBookingId()))
            service = groomingTypes.get(b.getBookingId());
        else if (boardingIds.contains(b.getBookingId()))
            service = "Boarding";
        else
            service = "Grooming";
        m.put("service", service);

        // Kennel info for boarding
        if (boardingIds.contains(b.getBookingId())) {
            boardingBookingRepository.findById(b.getBookingId()).ifPresent(bb -> {
                m.put("kennelNo", bb.getKennelNo());
                m.put("checkoutDate", bb.getCheckoutDate() != null ? bb.getCheckoutDate().toString() : null);
            });
        }

        return m;
    }
}
