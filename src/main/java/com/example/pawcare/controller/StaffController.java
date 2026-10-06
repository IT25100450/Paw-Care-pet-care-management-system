package com.example.pawcare.controller;

import com.example.pawcare.model.Role;
import com.example.pawcare.model.Staff;
import com.example.pawcare.repository.RoleRepository;
import com.example.pawcare.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public List<Map<String, Object>> getAllStaff() {
        return staffRepository.findAllWithRole().stream().map(this::toView).collect(Collectors.toList());
    }

    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createStaff(@RequestBody Map<String, Object> payload) {
        try {
            String email = (String) payload.get("email");
            String password = (String) payload.get("password");
            String contactNo = (String) payload.get("contactNo");
            Integer roleId = ((Number) payload.get("roleId")).intValue();

            if (!contactNo.matches("^\\d{10}$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Contact number must be exactly 10 digits"));
            }
            if (!email.toLowerCase().endsWith("@pawcare.com")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Staff email must end with @pawcare.com"));
            }

            if (staffRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            Role role = roleRepository.findById(roleId).orElse(null);
            if (role == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role ID"));
            }

            Staff staff = new Staff();
            staff.setEmail(email);
            staff.setPassword(password); // In a real app, hash the password
            staff.setContactNo(contactNo);
            staff.setRole(role);

            Staff saved = staffRepository.save(staff);
            return ResponseEntity.ok(toView(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create staff"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        try {
            Staff staff = staffRepository.findById(id).orElse(null);
            if (staff == null) {
                return ResponseEntity.notFound().build();
            }

            if (payload.containsKey("email")) {
                String email = (String) payload.get("email");
                if (!email.toLowerCase().endsWith("@pawcare.com")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Staff email must end with @pawcare.com"));
                }
                staff.setEmail(email);
            }
            if (payload.containsKey("password") && !((String) payload.get("password")).isEmpty()) {
                staff.setPassword((String) payload.get("password"));
            }
            if (payload.containsKey("contactNo")) {
                String contactNo = (String) payload.get("contactNo");
                if (!contactNo.matches("^\\d{10}$")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Contact number must be exactly 10 digits"));
                }
                staff.setContactNo(contactNo);
            }
            
            if (payload.containsKey("roleId")) {
                Integer roleId = ((Number) payload.get("roleId")).intValue();
                Role role = roleRepository.findById(roleId).orElse(null);
                if (role != null) staff.setRole(role);
            }

            Staff updated = staffRepository.save(staff);
            return ResponseEntity.ok(toView(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update staff"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Integer id) {
        if (!staffRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        staffRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private Map<String, Object> toView(Staff s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("staffId", s.getStaffId());
        map.put("email", s.getEmail());
        map.put("contactNo", s.getContactNo());
        map.put("roleId", s.getRole() != null ? s.getRole().getRoleId() : null);
        map.put("roleName", s.getRole() != null ? s.getRole().getRoleName() : null);
        return map;
    }
}
