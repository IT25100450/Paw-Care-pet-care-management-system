package com.example.pawcare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Staff_ID")
    private Integer staffId;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "Password", nullable = false, length = 255)
    private String password;

    @Column(name = "Contact_No", length = 10)
    private String contactNo;

    @ManyToOne
    @JoinColumn(name = "Role_ID", nullable = false)
    private Role role;

    public Staff() {}

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
