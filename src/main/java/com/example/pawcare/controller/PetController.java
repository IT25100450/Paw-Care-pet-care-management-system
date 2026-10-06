package com.example.pawcare.controller;

import com.example.pawcare.model.*;
import com.example.pawcare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    @Autowired private PetRepository petRepository;
    @Autowired private PetOwnerRepository petOwnerRepository;
    @Autowired private SpecieRepository specieRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private VetAppointmentRepository vetAppointmentRepository;
    @Autowired private GroomingBookingRepository groomingBookingRepository;
    @Autowired private BoardingBookingRepository boardingBookingRepository;

    /* ── GET all pets ────────────────────────────────────────────── */
    @GetMapping
    public List<Map<String, Object>> getAll() {
        return petRepository.findAllWithDetails().stream().map(this::toView).collect(Collectors.toList());
    }

    /* ── GET pets by owner ───────────────────────────────────────── */
    @GetMapping("/owner/{ownerId}")
    public List<Map<String, Object>> getByOwner(@PathVariable Integer ownerId) {
        return petRepository.findByOwnerOwnerId(ownerId).stream().map(this::toView).collect(Collectors.toList());
    }

    /* ── GET pets by owner email ─────────────────────────────────── */
    @GetMapping("/by-email")
    public List<Map<String, Object>> getByEmail(@RequestParam String email) {
        PetOwner owner = petOwnerRepository.findByEmail(email).orElse(null);
        if (owner == null) return List.of();
        return petRepository.findByOwnerOwnerId(owner.getOwnerId()).stream().map(this::toView).collect(Collectors.toList());
    }

    /* ── GET unified pet history ─────────────────────────────────── */
    @GetMapping("/{petId}/history")
    public ResponseEntity<?> getPetHistory(@PathVariable Integer petId) {
        if (!petRepository.existsById(petId)) return ResponseEntity.notFound().build();

        List<Booking> bookings = bookingRepository.findByPetPetId(petId);
        List<VetAppointment> vetAppts = vetAppointmentRepository.findByPetPetId(petId);

        Map<String, Object> history = new LinkedHashMap<>();
        history.put("services", bookings.stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", b.getBookingId());
            boolean isGrooming = groomingBookingRepository.existsById(b.getBookingId());
            boolean isBoarding = boardingBookingRepository.existsById(b.getBookingId());
            m.put("type", isGrooming ? "Grooming" : (isBoarding ? "Boarding" : "General Service"));
            m.put("date", b.getBookingDateTime() != null ? b.getBookingDateTime().toString() : null);
            m.put("status", b.getStatus());
            return m;
        }).collect(Collectors.toList()));

        history.put("veterinary", vetAppts.stream().map(va -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", va.getAppointmentId());
            m.put("date", va.getAppointmentDateTime() != null ? va.getAppointmentDateTime().toString() : null);
            m.put("reason", va.getReason());
            m.put("status", va.getStatus());
            m.put("vet", va.getVet() != null ? va.getVet().getEmail() : "Unassigned");
            return m;
        }).collect(Collectors.toList()));

        return ResponseEntity.ok(history);
    }

    /* ── POST create pet ─────────────────────────────────────────── */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Integer ownerId  = ((Number) body.get("ownerId")).intValue();
            Integer specieId = ((Number) body.get("specieId")).intValue();
            String name      = (String) body.get("name");
            String dob       = (String) body.get("dateOfBirth");

            PetOwner owner = petOwnerRepository.findById(ownerId).orElse(null);
            if (owner == null) return ResponseEntity.badRequest().body(Map.of("error", "Owner not found"));
            Specie specie = specieRepository.findById(specieId).orElse(null);
            if (specie == null) return ResponseEntity.badRequest().body(Map.of("error", "Species not found"));

            Pet pet = new Pet();
            pet.setName(name);
            pet.setOwner(owner);
            pet.setSpecie(specie);
            if (dob != null && !dob.isBlank()) pet.setDateOfBirth(LocalDate.parse(dob));

            return ResponseEntity.ok(toView(petRepository.save(pet)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /* ── PUT update pet ──────────────────────────────────────────── */
    @PutMapping("/{petId}")
    public ResponseEntity<?> update(@PathVariable Integer petId, @RequestBody Map<String, Object> body) {
        Pet pet = petRepository.findById(petId).orElse(null);
        if (pet == null) return ResponseEntity.notFound().build();

        if (body.get("name") != null) pet.setName((String) body.get("name"));
        if (body.get("dateOfBirth") != null) {
            String dob = (String) body.get("dateOfBirth");
            pet.setDateOfBirth(dob.isBlank() ? null : LocalDate.parse(dob));
        }
        if (body.get("specieId") != null) {
            Integer specieId = ((Number) body.get("specieId")).intValue();
            specieRepository.findById(specieId).ifPresent(pet::setSpecie);
        }
        return ResponseEntity.ok(toView(petRepository.save(pet)));
    }

    /* ── DELETE pet ──────────────────────────────────────────────── */
    @DeleteMapping("/{petId}")
    public ResponseEntity<?> delete(@PathVariable Integer petId) {
        if (!petRepository.existsById(petId)) return ResponseEntity.notFound().build();
        petRepository.deleteById(petId);
        return ResponseEntity.ok(Map.of("deleted", petId));
    }

    private Map<String, Object> toView(Pet p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("petId",      p.getPetId());
        m.put("name",       p.getName());
        m.put("dateOfBirth", p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : null);
        m.put("specieName", p.getSpecie() != null ? p.getSpecie().getSpecieName() : null);
        m.put("specieId",   p.getSpecie() != null ? p.getSpecie().getSpecieId() : null);
        if (p.getOwner() != null) {
            m.put("ownerId",    p.getOwner().getOwnerId());
            m.put("ownerName",  p.getOwner().getFirstName() + " " + p.getOwner().getLastName());
        }
        return m;
    }
}
