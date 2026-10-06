package com.example.pawcare.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Pet")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Pet_ID")
    private Integer petId;

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "DateOfBirth")
    private LocalDate dateOfBirth;

    @ManyToOne
    @JoinColumn(name = "Owner_ID", nullable = false)
    private PetOwner owner;

    @ManyToOne
    @JoinColumn(name = "Specie_ID", nullable = false)
    private Specie specie;

    public Pet() {}

    public Integer getPetId() { return petId; }
    public void setPetId(Integer petId) { this.petId = petId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public PetOwner getOwner() { return owner; }
    public void setOwner(PetOwner owner) { this.owner = owner; }

    public Specie getSpecie() { return specie; }
    public void setSpecie(Specie specie) { this.specie = specie; }
}
