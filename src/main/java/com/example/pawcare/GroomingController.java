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
@RequestMapping("/api/grooming")
public class GroomingController {

    @Autowired private GroomingBookingRepository groomingBookingRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private PetOwnerRepository petOwnerRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private GroomerRepository groomerRepository;

    /* ── GET all grooming bookings ─────────────────────────────── */
    @GetMapping
    public List<Map<String, Object>> getAll() {
        return groomingBookingRepository.findAllWithDetails().stream()
                .map(this::toView).collect(Collectors.toList());
    }

    /* ── PATCH assign groomer + service type to a grooming booking ─ */
    @PatchMapping("/{bookingId}/assign")
    public ResponseEntity<?> assignGroomer(@PathVariable Integer bookingId, @RequestBody Map<String, Object> body) {
        GroomingBooking gb = groomingBookingRepository.findById(bookingId).orElse(null);
        if (gb == null) return ResponseEntity.notFound().build();

        String current = gb.getStatus() == null ? "" : gb.getStatus().toLowerCase();
        if (current.equals("completed") || current.equals("cancelled")) {
            return ResponseEntity.status(409).body(Map.of("error",
                    "This grooming session is " + current + " and can no longer be changed."));
        }

        if (body.get("groomerId") != null) {
            Integer groomerId = ((Number) body.get("groomerId")).intValue();
            Groomer groomer = groomerRepository.findById(groomerId).orElse(null);
            if (groomer == null) return ResponseEntity.badRequest().body(Map.of("error", "Groomer not found"));

            LocalDateTime slot = gb.getBooking().getBookingDateTime();
            if (slot != null) {
                List<GroomingBooking> clashes = groomingBookingRepository.findGroomerConflicts(
                        groomerId, bookingId, slot.minusHours(2), slot.plusHours(2));
                if (!clashes.isEmpty()) {
                    LocalDateTime other = clashes.get(0).getBooking().getBookingDateTime();
                    return ResponseEntity.status(409).body(Map.of("error",
                            groomer.getFirstName() + " " + groomer.getLastName()
                            + " already has a confirmed session at " + other.toString().replace('T', ' ')
                            + ". Pick another groomer or time (2-hour gap required)."));
                }
            }
            gb.setGroomer(groomer);
        }

        if (body.get("serviceType") != null) {
            gb.setType((String) body.get("serviceType"));
        }

        gb.setStatus("confirmed");
        gb.getBooking().setStatus("confirmed");
        bookingRepository.save(gb.getBooking());
        groomingBookingRepository.save(gb);
        return ResponseEntity.ok(toView(gb));
    }

    /* ── POST create grooming booking (used by customer booking flow) */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Integer ownerId = ((Number) body.get("ownerId")).intValue();
            Integer petId   = ((Number) body.get("petId")).intValue();
            String dateTime = (String) body.get("bookingDateTime");
            String type     = (String) body.get("type");

            PetOwner owner = petOwnerRepository.findById(ownerId).orElse(null);
            if (owner == null) return ResponseEntity.badRequest().body(Map.of("error", "Owner not found"));
            Pet pet = petRepository.findById(petId).orElse(null);
            if (pet == null) return ResponseEntity.badRequest().body(Map.of("error", "Pet not found"));

            Booking booking = new Booking();
            booking.setOwner(owner);
            booking.setPet(pet);
            booking.setBookingDateTime(LocalDateTime.parse(dateTime));
            booking.setStatus("pending");
            Booking savedBooking = bookingRepository.save(booking);

            GroomingBooking gb = new GroomingBooking();
            gb.setBooking(savedBooking);
            gb.setType(type);
            gb.setStatus("pending");
            groomingBookingRepository.save(gb);

            return ResponseEntity.ok(toView(gb));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /* ── GROOMER MANAGEMENT (CRUD) ─────────────────────────────── */
    @GetMapping("/groomers")
    public List<Groomer> getAllGroomers() { return groomerRepository.findAll(); }

    @PostMapping("/groomers")
    public ResponseEntity<?> addGroomer(@RequestBody Groomer groomer) {
        if (groomerRepository.findByEmail(groomer.getEmail()).isPresent())
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        return ResponseEntity.ok(groomerRepository.save(groomer));
    }

    @PutMapping("/groomers/{id}")
    public ResponseEntity<?> updateGroomer(@PathVariable Integer id, @RequestBody Groomer updated) {
        Groomer g = groomerRepository.findById(id).orElse(null);
        if (g == null) return ResponseEntity.notFound().build();
        if (updated.getFirstName() != null)  g.setFirstName(updated.getFirstName());
        if (updated.getLastName() != null)   g.setLastName(updated.getLastName());
        if (updated.getContactNo() != null)  g.setContactNo(updated.getContactNo());
        if (updated.getSpecialty() != null)  g.setSpecialty(updated.getSpecialty());
        return ResponseEntity.ok(groomerRepository.save(g));
    }

    @DeleteMapping("/groomers/{id}")
    public ResponseEntity<?> deleteGroomer(@PathVariable Integer id) {
        if (!groomerRepository.existsById(id)) return ResponseEntity.notFound().build();
        groomerRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }

    private Map<String, Object> toView(GroomingBooking gb) {
        Booking b = gb.getBooking();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("bookingId",       b.getBookingId());
        m.put("bookingDateTime", b.getBookingDateTime() != null ? b.getBookingDateTime().toString() : null);
        m.put("type",            gb.getType());
        m.put("status",          gb.getStatus());
        m.put("ownerName",       b.getOwner().getFirstName() + " " + b.getOwner().getLastName());
        m.put("ownerEmail",      b.getOwner().getEmail());
        m.put("ownerId",         b.getOwner().getOwnerId());
        m.put("petName",         b.getPet().getName());
        m.put("petId",           b.getPet().getPetId());
        if (gb.getGroomer() != null) {
            m.put("groomerId",   gb.getGroomer().getGroomerId());
            m.put("groomerName", gb.getGroomer().getFirstName() + " " + gb.getGroomer().getLastName());
        } else {
            m.put("groomerId",   null);
            m.put("groomerName", "Unassigned");
        }
        return m;
    }
}
