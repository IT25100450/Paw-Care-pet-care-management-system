package com.example.pawcare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Vet")
public class Vet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Vet_ID")
    private Integer vetId;

    @Column(name = "First_Name", length = 50)
    private String firstName;

    @Column(name = "Last_Name", length = 50)
    private String lastName;

    @Column(name = "Email", length = 100, unique = true)
    private String email;

    @Column(name = "Contact_No", length = 20)
    private String contactNo;

    @Column(name = "Specialization", length = 100)
    private String specialization;

    public Vet() {}

    public Integer getVetId() { return vetId; }
    public void setVetId(Integer vetId) { this.vetId = vetId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
