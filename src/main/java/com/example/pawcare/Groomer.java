package com.example.pawcare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Groomer")
public class Groomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Groomer_ID")
    private Integer groomerId;

    @Column(name = "First_Name", length = 50)
    private String firstName;

    @Column(name = "Last_Name", length = 50)
    private String lastName;

    @Column(name = "Email", length = 100, unique = true)
    private String email;

    @Column(name = "Contact_No", length = 20)
    private String contactNo;

    @Column(name = "Specialty", length = 100)
    private String specialty;

    public Groomer() {}

    public Integer getGroomerId() { return groomerId; }
    public void setGroomerId(Integer groomerId) { this.groomerId = groomerId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
}
