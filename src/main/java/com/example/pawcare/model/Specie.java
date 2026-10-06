package com.example.pawcare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Specie")
public class Specie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Specie_ID")
    private Integer specieId;

    @Column(name = "Specie_name", nullable = false, length = 50)
    private String specieName;

    public Specie() {}

    public Integer getSpecieId() { return specieId; }
    public void setSpecieId(Integer specieId) { this.specieId = specieId; }

    public String getSpecieName() { return specieName; }
    public void setSpecieName(String specieName) { this.specieName = specieName; }
}
