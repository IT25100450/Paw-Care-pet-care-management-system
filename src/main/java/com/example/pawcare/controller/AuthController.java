package com.example.pawcare.controller;

import com.example.pawcare.model.PetOwner;
import com.example.pawcare.model.Staff;
import com.example.pawcare.repository.PetOwnerRepository;
import com.example.pawcare.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.pawcare.dto.SignupRequestDTO;
import com.example.pawcare.dto.LoginRequestDTO;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private PetOwnerRepository petOwnerRepository;
    @Autowired private StaffRepository staffRepository;

    /* ── POST /api/auth/signup ────────────────────────────────────── */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDTO request) {
        String firstName = request.getFirstName();
        String lastName  = request.getLastName();
        String email     = request.getEmail();
        String password  = request.getPassword();
        String contactNo = request.getContactNo();

        if (firstName == null || lastName == null || email == null || password == null || contactNo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
        }
        if (!contactNo.matches("^\\d{10}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Contact number must be exactly 10 digits"));
        }
        if (!email.toLowerCase().endsWith("@gmail.com")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Customer email must be a valid @gmail.com address"));
        }
        if (firstName.length() > 50 || lastName.length() > 50 || email.length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name or email exceeds maximum length"));
        }

        if (petOwnerRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        try {
            PetOwner owner = new PetOwner();
            owner.setFirstName(firstName);
            owner.setLastName(lastName);
            owner.setEmail(email);
            owner.setPassword(password);
            owner.setContactNo(contactNo);
            owner.setHouseNo(request.getHouseNo() != null ? request.getHouseNo() : "");
            owner.setStreetName(request.getStreetName() != null ? request.getStreetName() : "");
            owner.setCity(request.getCity() != null ? request.getCity() : "");
            owner.setProvince(request.getProvince() != null ? request.getProvince() : "");
            owner.setPostalCode(request.getPostalCode() != null ? request.getPostalCode() : "");

            PetOwner saved = petOwnerRepository.save(owner);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userType", "customer");
            response.put("ownerId", saved.getOwnerId());
            response.put("firstName", saved.getFirstName());
            response.put("lastName", saved.getLastName());
            response.put("email", saved.getEmail());
            response.put("contactNo", saved.getContactNo());
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid data format or length exceeded."));
        }
    }

    /* ── POST /api/auth/login ─────────────────────────────────────── */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        String email    = request.getEmail();
        String password = request.getPassword();

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        /* Try Pet Owner first */
        var ownerOpt = petOwnerRepository.findByEmailAndPassword(email, password);
        if (ownerOpt.isPresent()) {
            PetOwner owner = ownerOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userType", "customer");
            response.put("ownerId", owner.getOwnerId());
            response.put("firstName", owner.getFirstName());
            response.put("lastName", owner.getLastName());
            response.put("email", owner.getEmail());
            response.put("contactNo", owner.getContactNo());
            return ResponseEntity.ok(response);
        }

        /* Try Staff */
        var staffOpt = staffRepository.findByEmailAndPasswordWithRole(email, password);
        if (staffOpt.isPresent()) {
            if (!email.toLowerCase().endsWith("@pawcare.com")) {
                return ResponseEntity.status(401).body(Map.of("error", "Staff login requires a @pawcare.com email address"));
            }
            Staff staff = staffOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userType", "staff");
            response.put("staffId", staff.getStaffId());
            response.put("email", staff.getEmail());
            response.put("roleName", staff.getRole().getRoleName());
            response.put("roleId", staff.getRole().getRoleId());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
    }

    /* ── GET /api/auth/profile/{ownerId} ─────────────────────────── */
    @GetMapping("/profile/{ownerId}")
    public ResponseEntity<?> getProfile(@PathVariable Integer ownerId) {
        return petOwnerRepository.findById(ownerId)
                .map(owner -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("ownerId", owner.getOwnerId());
                    response.put("firstName", owner.getFirstName());
                    response.put("lastName", owner.getLastName());
                    response.put("email", owner.getEmail());
                    response.put("contactNo", owner.getContactNo());
                    response.put("houseNo", owner.getHouseNo());
                    response.put("streetName", owner.getStreetName());
                    response.put("city", owner.getCity());
                    response.put("province", owner.getProvince());
                    response.put("postalCode", owner.getPostalCode());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
