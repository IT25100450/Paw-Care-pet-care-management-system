package com.example.pawcare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Pet_Owner")
public class PetOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Owner_ID")
    private Integer ownerId;

    @Column(name = "First_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "Last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "Password", nullable = false, length = 255)
    private String password;

    @Column(name = "Contact_No", nullable = false, length = 10)
    private String contactNo;

    @Column(name = "House_No", length = 20)
    private String houseNo;

    @Column(name = "Street_name", length = 30)
    private String streetName;

    @Column(name = "City", length = 25)
    private String city;

    @Column(name = "Province", length = 25)
    private String province;

    @Column(name = "Postal_code", length = 20)
    private String postalCode;

    public PetOwner() {}

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getHouseNo() { return houseNo; }
    public void setHouseNo(String houseNo) { this.houseNo = houseNo; }

    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
}
