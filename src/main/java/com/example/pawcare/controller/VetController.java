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
@RequestMapping("/api/vet")
public class VetController {

    @Autowired private VetAppointmentRepository vetAppointmentRepository;
    @Autowired private MedicalRecordRepository medicalRecordRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private VetRepository vetRepository;
    @Autowired private MedicalRecordVaccinationRepository vaccinationRepository;

    /* ── GET all vet appointments ────────────────────────────────── */
    @GetMapping("/appointments")
    public List<Map<String, Object>> getAllAppointments() {
        return vetAppointmentRepository.findAllWithDetails().stream()
                .map(this::toAppointmentView).collect(Collectors.toList());
    }

    /* ── GET appointments by owner ───────────────────────────────── */
    @GetMapping("/appointments/owner/{ownerId}")
    public List<Map<String, Object>> getByOwner(@PathVariable Integer ownerId) {
        return vetAppointmentRepository.findByPetOwnerOwnerId(ownerId).stream()
                .map(this::toAppointmentView).collect(Collectors.toList());
    }

    /* ── GET available vets for a time slot ──────────────────────── */
    @GetMapping("/available-vets")
    public List<Map<String, Object>> getAvailableVets(@RequestParam String dateTime) {
        LocalDateTime dt = LocalDateTime.parse(dateTime);
        LocalDateTime windowStart = dt.minusMinutes(30);
        LocalDateTime windowEnd   = dt.plusMinutes(90);

        List<Vet> allVets = vetRepository.findAll();

        Set<Integer> busyVetIds = vetAppointmentRepository
                .findByAppointmentDateTimeBetween(windowStart, windowEnd)
                .stream()
                .filter(va -> va.getVet() != null && "confirmed".equals(va.getStatus()))
                .map(va -> va.getVet().getVetId())
                .collect(Collectors.toSet());

        return allVets.stream().map(vet -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("vetId", vet.getVetId());
            m.put("name", vet.getFirstName() + " " + vet.getLastName());
            m.put("specialization", vet.getSpecialization());
            boolean busy = busyVetIds.contains(vet.getVetId());
            m.put("available", !busy);
            m.put("statusLabel", busy ? "Not available" : "Available");
            return m;
        }).collect(Collectors.toList());
    }

    /* ── POST create vet appointment (customer — no vet assigned yet) */
    @PostMapping("/appointments")
    public ResponseEntity<?> createAppointment(@RequestBody Map<String, Object> body) {
        try {
            Integer petId = ((Number) body.get("petId")).intValue();
            String dateTime = (String) body.get("appointmentDateTime");
            String reason   = (String) body.get("reason");

            Pet pet = petRepository.findById(petId).orElse(null);
            if (pet == null) return ResponseEntity.badRequest().body(Map.of("error", "Pet not found"));

            // One open appointment per pet: block if a pending/confirmed one already exists
            Optional<VetAppointment> open = vetAppointmentRepository.findAllWithDetails().stream()
                    .filter(x -> x.getPet().getPetId().equals(petId))
                    .filter(x -> {
                        String s = x.getStatus() == null ? "pending" : x.getStatus().toLowerCase();
                        return s.equals("pending") || s.equals("confirmed");
                    })
                    .findFirst();
            if (open.isPresent()) {
                VetAppointment ex = open.get();
                return ResponseEntity.status(409).body(Map.of("error",
                        pet.getName() + " already has a " + ex.getStatus() + " vet appointment (#" + ex.getAppointmentId()
                        + " on " + String.valueOf(ex.getAppointmentDateTime()).replace('T', ' ')
                        + "). It must be completed or cancelled before booking another."));
            }

            VetAppointment appointment = new VetAppointment();
            appointment.setPet(pet);
            appointment.setVet(null);  // assigned by coordinator when confirming
            appointment.setAppointmentDateTime(LocalDateTime.parse(dateTime));
            appointment.setReason(reason);
            appointment.setStatus("pending");

            VetAppointment saved = vetAppointmentRepository.save(appointment);
            return ResponseEntity.ok(toAppointmentView(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /* ── PATCH confirm appointment (coordinator assigns vet) ────── */
    @PatchMapping("/appointments/{id}")
    public ResponseEntity<?> patchAppointment(@PathVariable Integer id,
                                               @RequestBody Map<String, Object> body) {
        VetAppointment va = vetAppointmentRepository.findById(id).orElse(null);
        if (va == null) return ResponseEntity.notFound().build();

        String status = (String) body.get("status");
        if (status != null) va.setStatus(status);

        if (body.get("vetId") != null) {
            Integer vetId = ((Number) body.get("vetId")).intValue();
            Vet vet = vetRepository.findById(vetId).orElse(null);
            if (vet == null) return ResponseEntity.badRequest().body(Map.of("error", "Vet not found: " + vetId));

            // Check if vet is already busy in that time slot (exclude this appointment)
            LocalDateTime windowStart = va.getAppointmentDateTime().minusMinutes(30);
            LocalDateTime windowEnd = va.getAppointmentDateTime().plusMinutes(90);
            boolean isBusy = vetAppointmentRepository.findByAppointmentDateTimeBetween(windowStart, windowEnd)
                .stream()
                .anyMatch(otherVa -> !otherVa.getAppointmentId().equals(va.getAppointmentId()) 
                                     && otherVa.getVet() != null 
                                     && otherVa.getVet().getVetId().equals(vetId)
                                     && "confirmed".equals(otherVa.getStatus()));
            if (isBusy) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vet is already booked for this time slot."));
            }

            va.setVet(vet);
        }

        VetAppointment saved = vetAppointmentRepository.save(va);
        return ResponseEntity.ok(toAppointmentView(saved));
    }

    /* ── DELETE appointment ───────────────────────────────────────── */
    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Integer id) {
        if (!vetAppointmentRepository.existsById(id)) return ResponseEntity.notFound().build();
        // Medical record must be deleted first if it exists
        medicalRecordRepository.findByAppointmentId(id).ifPresent(medicalRecordRepository::delete);
        vetAppointmentRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }

    /* ── GET all medical records ──────────────────────────────────── */
    @GetMapping("/records")
    public List<Map<String, Object>> getAllRecords() {
        return medicalRecordRepository.findAllWithDetails().stream()
                .map(this::toRecordView).collect(Collectors.toList());
    }

    /* ── POST create medical record with vaccinations ─────────────── */
    @PostMapping("/records")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> createRecord(@RequestBody Map<String, Object> body) {
        try {
            Integer appointmentId = ((Number) body.get("appointmentId")).intValue();
            VetAppointment appointment = vetAppointmentRepository.findById(appointmentId).orElse(null);
            if (appointment == null) return ResponseEntity.badRequest().body(Map.of("error", "Appointment not found"));

            MedicalRecord record = new MedicalRecord();
            record.setAppointment(appointment);
            record.setDiagnosis((String) body.get("diagnosis"));
            record.setTreatmentProvided((String) body.get("treatmentProvided"));
            record.setFollowUpRecommendations((String) body.get("followUpRecommendations"));
            record.setPrescribedMedication((String) body.get("prescribedMedication"));

            MedicalRecord saved = medicalRecordRepository.save(record);

            // Visit is done once its medical record is logged
            appointment.setStatus("completed");
            vetAppointmentRepository.save(appointment);

            // Handle Vaccinations
            List<Map<String, Object>> vaccinations = (List<Map<String, Object>>) body.get("vaccinations");
            if (vaccinations != null && !vaccinations.isEmpty()) {
                for (Map<String, Object> vData : vaccinations) {
                    MedicalRecordVaccination v = new MedicalRecordVaccination();
                    v.setMedicalRecord(saved);
                    v.setTypeV((String) vData.get("typeV"));
                    String dateAdmin = (String) vData.get("dateAdministered");
                    String nextDue = (String) vData.get("nextDueDate");
                    if (dateAdmin != null && !dateAdmin.isBlank()) v.setDateAdministered(java.time.LocalDate.parse(dateAdmin));
                    if (nextDue != null && !nextDue.isBlank()) v.setNextDueDate(java.time.LocalDate.parse(nextDue));
                    vaccinationRepository.save(v);
                }
            }

            return ResponseEntity.ok(toRecordView(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /* ── DELETE medical record ────────────────────────────────────── */
    @DeleteMapping("/records/{id}")
    public ResponseEntity<?> deleteRecord(@PathVariable Integer id) {
        if (!medicalRecordRepository.existsById(id)) return ResponseEntity.notFound().build();
        vaccinationRepository.deleteByMedicalRecord_MedicalRecordId(id);
        medicalRecordRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }

    /* ── Helpers ─────────────────────────────────────────────────── */
    private Map<String, Object> toAppointmentView(VetAppointment va) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("appointmentId",       va.getAppointmentId());
        m.put("appointmentDateTime", va.getAppointmentDateTime() != null ? va.getAppointmentDateTime().toString() : null);
        m.put("reason",              va.getReason());
        m.put("status",              va.getStatus() != null ? va.getStatus() : "pending");
        m.put("petName",             va.getPet().getName());
        m.put("petId",               va.getPet().getPetId());
        if (va.getPet().getOwner() != null) {
            m.put("ownerName",  va.getPet().getOwner().getFirstName() + " " + va.getPet().getOwner().getLastName());
            m.put("ownerEmail", va.getPet().getOwner().getEmail());
            m.put("ownerId",    va.getPet().getOwner().getOwnerId());
        }
        if (va.getVet() != null) {
            m.put("vetId",    va.getVet().getVetId());
            m.put("vetName",  va.getVet().getFirstName() + " " + va.getVet().getLastName());
        } else {
            m.put("vetId",    null);
            m.put("vetName",  "Unassigned");
        }
        return m;
    }

    private Map<String, Object> toRecordView(MedicalRecord mr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("medicalRecordId",         mr.getMedicalRecordId());
        m.put("diagnosis",               mr.getDiagnosis());
        m.put("treatmentProvided",       mr.getTreatmentProvided());
        m.put("followUpRecommendations", mr.getFollowUpRecommendations());
        m.put("prescribedMedication",    mr.getPrescribedMedication());
        m.put("appointmentId",           mr.getAppointment().getAppointmentId());
        m.put("petName",                 mr.getAppointment().getPet().getName());

        List<MedicalRecordVaccination> vaxes = vaccinationRepository.findByMedicalRecord_MedicalRecordId(mr.getMedicalRecordId());
        List<Map<String, Object>> vaxList = vaxes.stream().map(v -> {
            Map<String, Object> vm = new LinkedHashMap<>();
            vm.put("typeV", v.getTypeV());
            vm.put("dateAdministered", v.getDateAdministered() != null ? v.getDateAdministered().toString() : null);
            vm.put("nextDueDate", v.getNextDueDate() != null ? v.getNextDueDate().toString() : null);
            return vm;
        }).collect(Collectors.toList());
        m.put("vaccinations", vaxList);

        return m;
    }

    /* ── VET MANAGEMENT (CRUD) ───────────────────────────────────── */
    @GetMapping("/vets")
    public List<Vet> getAllVets() {
        return vetRepository.findAll();
    }

    @PostMapping("/vets")
    public ResponseEntity<?> addVet(@RequestBody Vet vet) {
        if (vetRepository.findByEmail(vet.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }
        return ResponseEntity.ok(vetRepository.save(vet));
    }

    @PutMapping("/vets/{id}")
    public ResponseEntity<?> updateVet(@PathVariable Integer id, @RequestBody Vet updatedVet) {
        Vet vet = vetRepository.findById(id).orElse(null);
        if (vet == null) return ResponseEntity.notFound().build();

        if (updatedVet.getFirstName() != null) vet.setFirstName(updatedVet.getFirstName());
        if (updatedVet.getLastName() != null) vet.setLastName(updatedVet.getLastName());
        if (updatedVet.getContactNo() != null) vet.setContactNo(updatedVet.getContactNo());
        if (updatedVet.getSpecialization() != null) vet.setSpecialization(updatedVet.getSpecialization());
        
        return ResponseEntity.ok(vetRepository.save(vet));
    }

    @DeleteMapping("/vets/{id}")
    public ResponseEntity<?> deleteVet(@PathVariable Integer id) {
        if (!vetRepository.existsById(id)) return ResponseEntity.notFound().build();
        vetRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }
}
