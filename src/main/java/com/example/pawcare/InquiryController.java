package com.example.pawcare.controller;

import com.example.pawcare.model.Inquiry;
import com.example.pawcare.model.PetOwner;
import com.example.pawcare.model.Staff;
import com.example.pawcare.repository.InquiryRepository;
import com.example.pawcare.repository.PetOwnerRepository;
import com.example.pawcare.repository.StaffRepository;
import com.example.pawcare.repository.BookingRepository;
import com.example.pawcare.repository.VetAppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/support")
public class InquiryController {

    @Autowired private InquiryRepository inquiryRepository;
    @Autowired private PetOwnerRepository petOwnerRepository;
    @Autowired private StaffRepository staffRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private VetAppointmentRepository vetAppointmentRepository;

    @GetMapping("/inquiries")
    public List<Map<String, Object>> getAllInquiries() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toView).collect(Collectors.toList());
    }

    @GetMapping("/inquiries/owner/{ownerId}")
    public List<Map<String, Object>> getInquiriesByOwner(@PathVariable Integer ownerId) {
        return inquiryRepository.findByOwnerOwnerId(ownerId).stream().map(this::toView).collect(Collectors.toList());
    }

    @PostMapping("/inquiries")
    public ResponseEntity<?> submitInquiry(@RequestBody Map<String, Object> body) {
        try {
            Integer ownerId = ((Number) body.get("ownerId")).intValue();
            PetOwner owner = petOwnerRepository.findById(ownerId).orElse(null);
            if (owner == null) return ResponseEntity.badRequest().body(Map.of("error", "Owner not found"));

            Inquiry inq = new Inquiry();
            inq.setOwner(owner);
            inq.setSubject((String) body.get("subject"));
            inq.setMessage((String) body.get("message"));

            Inquiry saved = inquiryRepository.save(inq);
            return ResponseEntity.ok(toView(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to submit inquiry"));
        }
    }

    @PatchMapping("/inquiries/{id}/reply")
    public ResponseEntity<?> replyInquiry(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Inquiry inq = inquiryRepository.findById(id).orElse(null);
        if (inq == null) return ResponseEntity.notFound().build();

        Integer staffId = ((Number) body.get("staffId")).intValue();
        Staff staff = staffRepository.findById(staffId).orElse(null);
        if (staff == null) return ResponseEntity.badRequest().body(Map.of("error", "Staff not found"));

        inq.setResponse((String) body.get("response"));
        inq.setStatus("Answered");
        inq.setRespondedBy(staff);

        Inquiry saved = inquiryRepository.save(inq);
        return ResponseEntity.ok(toView(saved));
    }

    @GetMapping("/reports/services")
    public ResponseEntity<String> generateServiceReport() {
        StringBuilder csv = new StringBuilder();
        csv.append("Type,ID,Date,Status\n");

        bookingRepository.findAll().forEach(b -> {
            csv.append("Service Booking,")
               .append(b.getBookingId()).append(",")
               .append(b.getBookingDateTime()).append(",")
               .append(b.getStatus()).append("\n");
        });

        vetAppointmentRepository.findAll().forEach(v -> {
            csv.append("Vet Appointment,")
               .append(v.getAppointmentId()).append(",")
               .append(v.getAppointmentDateTime()).append(",")
               .append(v.getStatus()).append("\n");
        });

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=service_report.csv");
        headers.add("Content-Type", "text/csv");
        
        return ResponseEntity.ok().headers(headers).body(csv.toString());
    }

    private Map<String, Object> toView(Inquiry inq) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("inquiryId", inq.getInquiryId());
        m.put("subject", inq.getSubject());
        m.put("message", inq.getMessage());
        m.put("status", inq.getStatus());
        m.put("createdAt", inq.getCreatedAt() != null ? inq.getCreatedAt().toString() : null);
        m.put("response", inq.getResponse());
        
        if (inq.getOwner() != null) {
            m.put("ownerName", inq.getOwner().getFirstName() + " " + inq.getOwner().getLastName());
            m.put("ownerEmail", inq.getOwner().getEmail());
        }
        if (inq.getRespondedBy() != null) {
            m.put("respondedBy", inq.getRespondedBy().getEmail());
        }
        return m;
    }
}
